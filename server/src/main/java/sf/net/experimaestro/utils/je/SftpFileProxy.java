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

package sf.net.experimaestro.utils.je;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import sf.net.experimaestro.scheduler.Scheduler;

@Persistent(proxyFor = SftpFileObject.class)
public class SftpFileProxy implements PersistentProxy<SftpFileObject> {
    String path;

    @Override
    public void initializeProxy(SftpFileObject object) {
        path = object.getPublicURIString();
    }

    @Override
    public SftpFileObject convertProxy() {
        try {
            return (SftpFileObject) Scheduler.getVFSManager().resolveFile(path);
        } catch (FileSystemException e) {
            throw new RuntimeException("Cannot resolve URI: " + path);
        }
    }

}