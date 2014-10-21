package sf.net.experimaestro.connectors;

import com.sleepycat.persist.model.Persistent;
import org.apache.commons.vfs2.FileObject;
import sf.net.experimaestro.exceptions.LaunchException;
import sf.net.experimaestro.scheduler.Job;

import java.io.IOException;
import java.util.Map;

/**
 * Created by bpiwowar on 26/9/14.
 */
public abstract class AbstractCommandBuilder {
    /** The different input/output */
    protected AbstractProcessBuilder.Redirect input;
    protected AbstractProcessBuilder.Redirect output;
    protected AbstractProcessBuilder.Redirect error;

    /** The associated job */
    Job job;

    /** Working directory */
    FileObject directory;

    /** Whether this process should be bound to the Java process */
    boolean detach;

    /**  The environment */
    private Map<String, String> environment;

    public Map<String,String> environment() {
        return environment;
    }

    public void environment(Map<String,String> environment) {
        this.environment = environment;
    }

    public FileObject directory() {
        return directory;
    }

    public Job job() {
        return job;
    }

    public void job(Job job) {
        this.job = job;
    }


    public AbstractCommandBuilder directory(FileObject directory) {
        this.directory = directory;
        return this;
    }

    /**
     *  Start the process and return an Experimaestro process
     *  @return A valid {@linkplain sf.net.experimaestro.connectors.XPMProcess}
     */
    abstract public XPMProcess start() throws LaunchException, IOException;

    public AbstractCommandBuilder redirectError(AbstractProcessBuilder.Redirect destination) {
        if (!destination.isWriter())
            throw new IllegalArgumentException();
        this.error = destination;
        return this;
    }

    public AbstractCommandBuilder redirectInput(AbstractProcessBuilder.Redirect source) {
        if (!source.type.isReader())
            throw new IllegalArgumentException();
        this.input = source;
        return this;
    }

    public boolean detach() {
        return detach;
    }

    public AbstractCommandBuilder detach(boolean detach) {
        this.detach = detach;
        return this;
    }

    public AbstractCommandBuilder redirectOutput(Redirect destination) {
        if (!destination.isWriter())
            throw new IllegalArgumentException();
        this.output = destination;
        return this;
    }

    /**
     * Represents a process source of input or output
     */
    @Persistent
    static public class Redirect {
        private FileObject file;
        private String string;

        private Type type;

        public static final Redirect PIPE = new Redirect(Type.PIPE, null);
        public static final Redirect INHERIT = new Redirect(Type.INHERIT, null);

        private Redirect() {
            this.type = Type.INHERIT;
        }

        private Redirect(Type type, FileObject file) {
            this.type = type;
            this.file = file;
        }

        public boolean isWriter() {
            return type.isWriter();
        }

        public FileObject file() {
            return file;
        }

        public String string() {
            return string;
        }

        public Type type() {
            return type;
        }

        static public enum Type {
            READ, APPEND, WRITE, PIPE, INHERIT;

            public boolean isReader() {
                return this == READ || this == INHERIT || this == PIPE;
            }

            public boolean isWriter() {
                return this == APPEND || this == WRITE || this == PIPE || this == INHERIT;
            }
        }

        static public Redirect from(FileObject file) {
            return new Redirect(Type.READ, file);
        }

        static public Redirect append(FileObject file) {
            return new Redirect(Type.APPEND, file);
        }

        static public Redirect to(FileObject file) {
            return new Redirect(Type.WRITE, file);
        }
    }
}
