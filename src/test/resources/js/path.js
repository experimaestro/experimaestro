/*
 * Tests for the use of a path component
 *
 *  This file is part of experimaestro.
 *  Copyright (c) 2011 B. Piwowarski <benjamin@bpiwowar.net>
 *
 *  experimaestro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  experimaestro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



// Direct
xpm.command_line_job("/tmp/a",["/bin/ls"]);

// With path
xpm.command_line_job("/tmp/a",[path("/bin/ls")]);

// Test with Several components
var arg = <>{path("/Users/bpiwowar/workspace/experimaestro")}/resources</>;
xpm.command_line_job("/tmp/b",[path("/bin/ls"), arg]);
