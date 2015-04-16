package sf.net.experimaestro.scheduler;

/*
 * This file is part of experimaestro.
 * Copyright (c) 2014 B. Piwowarski <benjamin@bpiwowar.net>
 *
 * experimaestro is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * experimaestro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.json.simple.JSONObject;
import sf.net.experimaestro.connectors.Connector;
import sf.net.experimaestro.connectors.SingleHostConnector;
import sf.net.experimaestro.exceptions.ExperimaestroCannotOverwrite;
import sf.net.experimaestro.exceptions.XPMRuntimeException;
import sf.net.experimaestro.utils.FileNameTransformer;
import sf.net.experimaestro.utils.log.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.*;

import static java.lang.String.format;

/**
 * The most general type of object manipulated by the server (can be a server, a
 * task, or a data)
 *
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 */
@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "resources")
@DiscriminatorColumn(name = "resourceType", discriminatorType = DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "resources", indexes = @Index(columnList = "path"))
public abstract class Resource implements PostCommitListener {
    /**
     * Extension for the lock file
     */
    public static final FileNameTransformer LOCK_EXTENSION = new FileNameTransformer("", ".lock");

    // --- Resource description

    /**
     * Extension for the file that describes the state of the resource
     */
    public static final FileNameTransformer STATUS_EXTENSION = new FileNameTransformer("", ".state");

    /**
     * Extension used status mark a produced resource
     */
    public static final FileNameTransformer DONE_EXTENSION = new FileNameTransformer("", ".done");

    /**
     * Extension for the file containing the return code
     */
    public static final FileNameTransformer CODE_EXTENSION = new FileNameTransformer(".xpm.", ".code");


    // --- Values filled on demand

    /**
     * Extension for the file containing the script status run
     */
    public static final FileNameTransformer RUN_EXTENSION = new FileNameTransformer(".xpm.", ".run");

    /**
     * Extension for the standard output of a job
     */
    public static final FileNameTransformer OUT_EXTENSION = new FileNameTransformer("", ".out");


    // --- Values filled on doPostInit

    /**
     * Extension for the standard error of a job
     */
    public static final FileNameTransformer ERR_EXTENSION = new FileNameTransformer("", ".err");

    /**
     * Extension for the standard input of a job
     */
    public static final FileNameTransformer INPUT_EXTENSION = new FileNameTransformer(".xpm.input.", "");

    public static final String JOB_TYPE = "1";
    public static final String TOKEN_RESOURCE_TYPE = "2";

    final static private Logger LOGGER = Logger.getLogger();

    /**
     * The path with the connector
     */
    @Column(name = "path", updatable = false)
    protected Path path;

    /**
     * The connector
     */
    @JoinColumn(name = "connector", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    protected Connector connector;

    /**
     * The outgoing dependencies (resources that depend on this)
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "from")
    @MapKey(name = "to")
    private Map<Resource, Dependency> outgoingDependencies = new HashMap<>();

    /**
     * The ingoing dependencies (resources that we depend upon)
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "to")
    @MapKey(name = "from")
    private Map<Resource, Dependency> ingoingDependencies = new HashMap<>();

    /**
     * The resource ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long resourceID;

    /**
     * Version for optimistic locks
     */
    @Version
    @Column(name = "version")
    protected long version;

    /**
     * Comparator on the database ID
     */
    static public final Comparator<Resource> ID_COMPARATOR = new Comparator<Resource>() {
        @Override
        public int compare(Resource o1, Resource o2) {
            return Long.compare(o1.resourceID, o2.resourceID);
        }
    };

    /**
     * The resource state
     */
    @Column(name = "state")
    private ResourceState state = ResourceState.ON_HOLD;

    /**
     * Indicates that the resource is deleted
     */
    transient private boolean delete = false;

    /**
     * Keeps the state of the resource before saving
     */
    protected transient ResourceState oldState;

    /**
     * Called when deserializing from database
     */
    protected Resource() {
        LOGGER.trace("Constructor of resource [%s@%s]", System.identityHashCode(this), this);
    }

    public Resource(Connector connector, Path path) {
        this.connector = connector;
        this.path = path;
    }

    /**
     * Get a resource by locator
     *
     * @param em   The current entity manager
     * @param path The path of the resource
     * @return The resource or null if there is no such resource
     */
    public static Resource getByLocator(EntityManager em, Path path) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Resource> cq = cb.createQuery(Resource.class);
        Root<Resource> root = cq.from(Resource.class);
        cq.where(root.get("path").in(cb.parameter(Path.class, "path")));


        TypedQuery<Resource> query = em.createQuery(cq);
        query.setParameter("path", path);
        List<Resource> result = query.getResultList();
        assert result.size() <= 1;

        if (result.isEmpty())
            return null;

        return result.get(0);
    }

    @Override
    protected void finalize() {
        LOGGER.debug("Finalizing resource [%s@%s]", System.identityHashCode(this), this);
    }

    @Override
    public int hashCode() {
        return resourceID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Resource && resourceID == ((Resource) obj).resourceID;
    }

    @Override
    public String toString() {
        if (resourceID == null)
            return "R-";
        return format("R%d", resourceID);
    }

    /**
     * Returns a detailed description
     */
    public String toDetailedString() {
        return toString() + "@" + version;
    }

    /**
     * Get the ID
     */
    public Long getId() {
        return resourceID;
    }

    /**
     * Update the state of the resource by checking all that could have changed externally
     * <p>
     * Calls {@linkplain #doUpdateStatus()}.
     * If the update fails for some reason, then we just put the state into HOLD
     */
    final public boolean updateStatus() {
        try {
            boolean b = doUpdateStatus();
            return b;
        } catch (Exception e) {
            LOGGER.error(e, "Exception while updating status");
            return setState(ResourceState.ON_HOLD);
        }
    }

    /**
     * Do a full update of the state of this resource.
     * <p>
     * This implies looking at the disk status check for done/lock files, etc.
     *
     * @return True if the state was updated
     */
    protected boolean doUpdateStatus() throws Exception {
        return false;
    }

    public boolean isStored() {
        return resourceID != null;
    }

    /**
     * Checks whether this resource can be replaced
     *
     * @return
     */
    public boolean canBeReplaced() {
        return ResourceState.UPDATABLE_STATES.contains(state);
    }

    /**
     * Notifies the resource that something happened
     *
     * @param t
     * @param em
     * @param message The message
     */
    public void notify(Transaction t, EntityManager em, Message message) {
        switch (message.getType()) {
            case RESOURCE_REMOVED:
                resourceID = null;
                break;
        }
    }

    /**
     * The set of resources the resource is dependent upon
     */
    public Collection<Dependency> getDependencies() {
        return ingoingDependencies.values();
    }

    /**
     * The set of dependencies that are dependent on this resource
     */
    public Collection<Dependency> getOutgoingDependencies() {
        return outgoingDependencies.values();
    }

    /**
     * @return the generated
     */
    public boolean isGenerated() {
        return getState() == ResourceState.DONE;
    }

    /**
     * Get the task from
     *
     * @return The task unique from
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get the identifier of this task
     *
     * @return Return a stringified version of the path
     */
    public String getIdentifier() {
        return getPath().toString();
    }

    /**
     * Get the state of the resource
     *
     * @return The current state of the resource
     */
    final public ResourceState getState() {
        return state;
    }

    /**
     * Sets the state
     *
     * @param state
     * @return <tt>true</tt> if state changed, <tt>false</tt> otherwise
     */
    public boolean setState(ResourceState state) {
        if (this.state == state)
            return false;
        this.state = state;

        // FIXME: Should be done when the operation is committed
//        if (this.isStored()) {
//            Scheduler.get().notify(new SimpleMessage(Message.Type.STATE_CHANGED, this));
//        }
        return true;
    }

    /**
     * Creates a new dependency on this resource
     *
     * @param type The parameters for the dependency
     * @return a new dependency
     */
    public abstract Dependency createDependency(Object type);

    /**
     * Returns the main output file for this resource
     */
    public Path outputFile() throws FileSystemException {
        throw new IllegalAccessError("No output file for resources of type " + this.getClass());
    }

    /**
     * Writes an XML description of the resource
     *
     * @param out
     * @param config The configuration for printing
     * @deprecated Use {@linkplain #toJSON()}
     */
    @Deprecated
    public void printXML(PrintWriter out, PrintConfig config) {
        out.format("<div><b>Resource id</b>: %s</h2>", getPath());
        out.format("<div><b>Status</b>: %s</div>", getState());
    }

    /**
     * Get a JSON representation of the object
     *
     * @return
     * @throws IOException
     */
    public JSONObject toJSON() throws IOException {
        JSONObject object = new JSONObject();
        object.put("id", getId());
        object.put("status", getState().toString());
        return object;
    }

    public void clean() {
    }

    protected Dependency addIngoingDependency(Dependency dependency) {
        dependency.to = this;
        return this.ingoingDependencies.put(dependency.getFrom(), dependency);
    }

    /**
     * Called when the resource was persisted/updated and committed
     */
    public void stored() {
        // We switched from a stopped state to a non stopped state : notify dependents
        if (oldState != null && oldState.isUnactive() ^ state.isUnactive()) {
            Scheduler.get().addChangedResource(this);
        }
    }


    /**
     * Store a resource
     *
     * @param resource
     * @return The old resource, or null if there was nothing
     * @throws ExperimaestroCannotOverwrite If the old resource could not be overriden
     */
    synchronized public Resource put(Resource resource) throws ExperimaestroCannotOverwrite {
        // Get the group
        final boolean newResource = !resource.isStored();
        if (newResource) {
            resource.updateStatus();
        }

        // TODO implement
        final Resource old = null;
        if (old == null) {
            throw new NotImplementedException();
        }

        LOGGER.debug("Storing resource %s [%x@%s] in state %s", resource, System.identityHashCode(resource), resource.getId(), resource.getState());

        if (newResource) {
            final long id = resource.getId();
            LOGGER.debug("Adding a new resource [%s] in database [id=%d/%x]", resource, id, System.identityHashCode(resource));

            // Notify
            Scheduler.get().notify(new SimpleMessage(Message.Type.RESOURCE_ADDED, resource));
        }


        return old;
    }

    /**
     * Delete a resource
     *
     * @param recursive Delete dependent resources
     */
    public synchronized void delete(boolean recursive) {
        if (getState() == ResourceState.RUNNING)
            throw new XPMRuntimeException("Cannot delete the running task [%s]", this);

        Collection<Dependency> dependencies = getOutgoingDependencies();
        if (!dependencies.isEmpty()) {
            if (recursive) {
                for (Dependency dependency : dependencies) {
                    Resource dep = dependency.getTo();
                    if (dep != null) {
                        dep.delete(true);
                    }
                }
            } else
                throw new XPMRuntimeException("Cannot delete the resource %s: it has dependencies", this);
        }

        // Remove
        Transaction.run((em, t) -> {
            em.remove(this);
            SimpleMessage message = new SimpleMessage(Message.Type.RESOURCE_REMOVED, this);
            this.notify(t, em, message);
            notify(t, em, message);
        });

        this.delete = true;
    }

    public Path getFileWithExtension(FileNameTransformer extension) throws FileSystemException {
        return extension.transform(path);
    }

    final public void replaceBy(Resource resource) throws ExperimaestroCannotOverwrite {
        if (resource.getClass() != this.getClass()) {
            throw new ExperimaestroCannotOverwrite("Class %s and %s differ", resource.getClass(), this.getClass());
        }

        if (!resource.getPath().equals(this.path)) {
            throw new ExperimaestroCannotOverwrite("Path %s and %s differ", resource.getPath(), this.getPath());
        }

        doReplaceBy(resource);
    }

    protected void doReplaceBy(Resource resource) {
        final EntityManager em = Transaction.current().em();

        // Copy ingoing dependencies
        Map<Resource, Dependency> oldIngoing = this.ingoingDependencies;
        this.ingoingDependencies = new HashMap<>();

        // Re-add old matching dependencies
        oldIngoing.entrySet().stream()
                .forEach(entry -> {
                    final Dependency dependency = resource.ingoingDependencies.get(entry.getKey());
                    if (dependency != null) {
                        entry.getValue().replaceBy(dependency);
                        this.ingoingDependencies.put(entry.getKey(), entry.getValue());
                    }
                });

        // Add new dependencies
        resource.ingoingDependencies.entrySet().stream()
                .forEach(entry -> {
                    if (!oldIngoing.containsKey(entry.getKey())) {
                        entry.getValue().to = this;
                        this.ingoingDependencies.put(entry.getKey(), entry.getValue());
                    }
                });

        this.state = resource.state;
    }

    public Connector getConnector() {
        return connector;
    }

    /**
     * Returns the main connector associated with this resource
     *
     * @return a SingleHostConnector object
     */
    public final SingleHostConnector getMainConnector() {
        return getConnector().getMainConnector();
    }


    static private SharedLongLocks resourceLocks = new SharedLongLocks();

    public EntityLock lock(Transaction t, boolean exclusive) {
        return t.lock(resourceLocks, this.getId(), exclusive, 0);
    }

    public EntityLock lock(Transaction t, boolean exclusive, long timeout) {
        return t.lock(resourceLocks, this.getId(), exclusive, timeout);
    }

    /**
     * Lock a resource by ID
     *
     * @param transaction
     * @param resourceId
     * @param exclusive
     * @param timeout     Timeout
     */
    public static EntityLock lock(Transaction transaction, long resourceId, boolean exclusive, long timeout) {
        return transaction.lock(resourceLocks, resourceId, exclusive, timeout);
    }

    /**
     * Defines how printing should be done
     */
    static public class PrintConfig {
        public String detailURL;
    }

    @PostLoad
    protected void postLoad() {
        oldState = state;
        prepared = true;
        LOGGER.debug("Loaded %s (state %s) - version %d", this, state, version);
    }

    /**
     * Called after an INSERT or UPDATE
     */
    @PostUpdate
    @PostPersist
    protected void _post_update() {
        Transaction.current().registerPostCommit(this);
    }


    transient boolean prepared = false;

    public void save(Transaction transaction) {
        final EntityManager em = transaction.em();
        // Find the connector in the database
        if (connector != null && !em.contains(connector)) {
            // Add the connector
            final Connector other = em.find(Connector.class, connector.getIdentifier());
            if (other != null) {
                connector = other;
            }
        }


        // Lock all the dependencies
        // This avoids to miss any notification


        boolean dependenciesLocked = false;
        List<EntityLock> locks = new ArrayList<>();
        while (!dependenciesLocked) {
            // We loop until we get all the locks - using a timeout just in case
            for (Dependency dependency : getDependencies()) {
                final EntityLock lock = dependency.from.lock(transaction, false, 100);
                if (lock != null) {
                    for (EntityLock _lock : locks) {
                        _lock.close();
                    }
                    continue;
                }
                break;
            }
            dependenciesLocked = true;
        }

        for (Dependency dependency : getDependencies()) {
            if (!em.contains(dependency.from)) {
                dependency.from = em.find(Resource.class, dependency.from.getId());
            } else {
                em.refresh(dependency.from);
            }
        }

        prepared = true;
        em.persist(this);

        // Update the status
        updateStatus();
    }

    /**
     * Called before adding this entity to the database
     */
    @PrePersist
    protected void _pre_persist() {
        if (!prepared) {
            throw new AssertionError("The object has not been prepared");
        }
    }

    @Override
    public void postCommit(Transaction transaction) {
        if (delete) {
            clean();
        } else {
            LOGGER.info("Resource %s stored with version=%s", this, version);
            stored();
        }
    }
}