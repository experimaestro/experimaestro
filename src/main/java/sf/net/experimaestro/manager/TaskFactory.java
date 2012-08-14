/*
 * This file is part of experimaestro.
 * Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
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

package sf.net.experimaestro.manager;

import java.util.Map;

import static java.lang.String.format;


/**
 * Information about an experiment
 * 
 * @author B. Piwowarski
 */
public abstract class TaskFactory {
	/**
	 * The identifier of this experiment
	 */
	protected QName id;

	/**
	 * The version
	 */
	String version;

	/**
	 * The group
	 */
	String group;

	/**
	 * The repository
	 */
	private final Repository repository;

	/**
	 * The module
	 */
	Module module;

	/**
	 * Initialise a task
	 * 
	 * @param repository
	 * 
	 * @param id
	 *            The id of the task
	 * @param version
	 * @param group
	 */
	public TaskFactory(Repository repository, QName id, String version,
			String group) {
		this.repository = repository;
		this.id = id;
		this.version = version;
		this.group = group;
	}

	public Repository getRepository() {
		return repository;
	}

	/**
	 * Documentation in XHTML format
	 */
	public String getDocumentation() {
		return format("<p>No documentation found for experiment %s</p>", id);
	}

	/**
	 * Get the list of (potential) parameters
	 * 
	 * @return a map of mappings from a qualified name to a named parameter or
	 *         null if non existent
	 */
	abstract public Map<String, Input> getInputs();

	/**
	 * Get the list of (potential) parameters
	 * 
	 * @return a map of mappings from a qualified name to a named parameter or
	 *         null if non existent
	 */
	abstract public Map<String, QName> getOutputs();

	/**
	 * Creates a new experiment
	 */
	public abstract Task create();

	/**
	 * Returns the qualified name for this task
	 */
	public QName getId() {
		return id;
	}

	public Object getVersion() {
		return version;
	}

	/**
	 * Finish the initialisation of the factory
	 */
	protected void init() {

	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		if (this.module == module)
			return;
		if (this.module != null)
			this.module.remove(this);
		this.module = module;
		module.addFactory(this);
	}
}
