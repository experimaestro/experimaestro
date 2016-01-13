package net.bpiwowar.xpm.manager.experiments;

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

import org.apache.commons.lang.NotImplementedException;
import net.bpiwowar.xpm.manager.QName;
import net.bpiwowar.xpm.scheduler.Resource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A task contains resources and is linked to other dependent tasks
 */
public class TaskReference {
    long id;

    /**
     * The parents
     */
    private final Collection<TaskReference> parents = new ArrayList<>();

    /**
     * The children
     */
    private final Collection<TaskReference> children = new ArrayList<>();

    /**
     * The ID
     */
    QName taskId;

    /**
     * The experiment ID
     */
    Experiment experiment;

    /**
     * The associated resources
     */
    Collection<Resource> resources = new ArrayList<>();

    public TaskReference() {
    }

    public TaskReference(Experiment experiment, QName taskId) {
        this.experiment = experiment;
        this.taskId = taskId;
    }

    /**
     * Add a parent
     */
    public void addParent(TaskReference parent) {
        assert parent.id != -1;
        assert this.id != -1;

        parent.children.add(this);
        parents.add(parent);
    }

    /**
     * Associate a resource to this task reference
     *
     * @param resource The resource to add
     */
    public void add(Resource resource) {
        resources.add(resource);
    }

    /**
     * Save in database
     */
    public void persists() {
        throw new NotImplementedException();
    }
}
