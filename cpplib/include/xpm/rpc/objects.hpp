#ifndef _XPM_RPCOBJECTS_H
#define _XPM_RPCOBJECTS_H

#include <vector>
#include <xpm/rpc/utils.hpp>
#include <xpm/rpc/optional.hpp>

#ifdef SWIG
%shared_ptr(xpm::rpc::Resource);
%shared_ptr(xpm::rpc::ConnectorOptions);
%shared_ptr(xpm::rpc::Command);
%shared_ptr(xpm::rpc::OARLauncher);
%shared_ptr(xpm::rpc::SingleHostConnector);
%shared_ptr(xpm::rpc::Pipe);
%shared_ptr(xpm::rpc::ReadWriteDependency);
%shared_ptr(xpm::rpc::Job);
%shared_ptr(xpm::rpc::Functions);
%shared_ptr(xpm::rpc::LocalhostConnector);
%shared_ptr(xpm::rpc::TokenResource);
%shared_ptr(xpm::rpc::AbstractCommandComponent);
%shared_ptr(xpm::rpc::CommandPath);
%shared_ptr(xpm::rpc::JsonParameterFile);
%shared_ptr(xpm::rpc::SubCommand);
%shared_ptr(xpm::rpc::CommandOutput);
%shared_ptr(xpm::rpc::Connector);
%shared_ptr(xpm::rpc::OARParameters);
%shared_ptr(xpm::rpc::CommandComponent);
%shared_ptr(xpm::rpc::SSHOptions);
%shared_ptr(xpm::rpc::AbstractCommand);
%shared_ptr(xpm::rpc::ParameterFile);
%shared_ptr(xpm::rpc::XPM);
%shared_ptr(xpm::rpc::CommandString);
%shared_ptr(xpm::rpc::LauncherParameters);
%shared_ptr(xpm::rpc::DirectLauncher);
%shared_ptr(xpm::rpc::Path);
%shared_ptr(xpm::rpc::Namespace);
%shared_ptr(xpm::rpc::CommandLineTask);
%shared_ptr(xpm::rpc::Commands);
%shared_ptr(xpm::rpc::Launcher);
%shared_ptr(xpm::rpc::Dependency);
%shared_ptr(xpm::rpc::SSHConnector);
#endif
#if defined(SWIGJAVA) && defined(SWIG) 
%nspace xpm::rpc::Resource;
%nspace xpm::rpc::ConnectorOptions;
%nspace xpm::rpc::Command;
%nspace xpm::rpc::OARLauncher;
%nspace xpm::rpc::SingleHostConnector;
%nspace xpm::rpc::Pipe;
%nspace xpm::rpc::ReadWriteDependency;
%nspace xpm::rpc::Job;
%nspace xpm::rpc::Functions;
%nspace xpm::rpc::LocalhostConnector;
%nspace xpm::rpc::TokenResource;
%nspace xpm::rpc::AbstractCommandComponent;
%nspace xpm::rpc::CommandPath;
%nspace xpm::rpc::JsonParameterFile;
%nspace xpm::rpc::SubCommand;
%nspace xpm::rpc::CommandOutput;
%nspace xpm::rpc::Connector;
%nspace xpm::rpc::OARParameters;
%nspace xpm::rpc::CommandComponent;
%nspace xpm::rpc::SSHOptions;
%nspace xpm::rpc::AbstractCommand;
%nspace xpm::rpc::ParameterFile;
%nspace xpm::rpc::XPM;
%nspace xpm::rpc::CommandString;
%nspace xpm::rpc::LauncherParameters;
%nspace xpm::rpc::DirectLauncher;
%nspace xpm::rpc::Path;
%nspace xpm::rpc::Namespace;
%nspace xpm::rpc::CommandLineTask;
%nspace xpm::rpc::Commands;
%nspace xpm::rpc::Launcher;
%nspace xpm::rpc::Dependency;
%nspace xpm::rpc::SSHConnector;
#endif
namespace xpm { namespace rpc {


// Pre-declaration
class Resource;
class ConnectorOptions;
class Command;
class OARLauncher;
class SingleHostConnector;
class Pipe;
class ReadWriteDependency;
class Job;
class Functions;
class LocalhostConnector;
class TokenResource;
class AbstractCommandComponent;
class CommandPath;
class JsonParameterFile;
class SubCommand;
class CommandOutput;
class Connector;
class OARParameters;
class CommandComponent;
class SSHOptions;
class AbstractCommand;
class ParameterFile;
class XPM;
class CommandString;
class LauncherParameters;
class DirectLauncher;
class Path;
class Namespace;
class CommandLineTask;
class Commands;
class Launcher;
class Dependency;
class SSHConnector;


// Classes
class AbstractCommandComponent : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<AbstractCommandComponent>>;
  explicit AbstractCommandComponent(ObjectIdentifier o);
  AbstractCommandComponent() {}

public:
};

class Connector : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Connector>>;
  explicit Connector(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Connector() {}

public:
  /**   */
  virtual std::string resolve(std::shared_ptr<Path> const &path);
  /**   */
  static std::shared_ptr<Connector> create(std::string const &string, std::string const &string_1, std::shared_ptr<ConnectorOptions> const &connectorOptions = std::shared_ptr<ConnectorOptions>());
  /**   */
  virtual std::shared_ptr<SingleHostConnector> asSingleHostConnector();
  /**   */
  virtual std::shared_ptr<Launcher> default_launcher();
};

class Resource : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Resource>>;
  explicit Resource(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Resource() {}

public:
  /**   */
  virtual std::string toString();
  /**   */
  virtual std::shared_ptr<Path> resolve(std::string const &string);
  /**   */
  virtual std::shared_ptr<Path> file();
  /**   */
  virtual std::shared_ptr<Path> output();
  /**   */
  virtual void taskId(std::string const &string);
  /**   */
  virtual std::string taskId();
};

class Job : public Resource {
protected:
  friend struct RPCConverter<std::shared_ptr<Job>>;
  explicit Job(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Job() {}

public:
  /**   */
  virtual std::shared_ptr<Resource> submit();
};

class AbstractCommand : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<AbstractCommand>>;
  explicit AbstractCommand(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  AbstractCommand() {}

public:
  /**   */
  virtual std::shared_ptr<CommandOutput> output();
  /**   */
  virtual void add_dependency(std::shared_ptr<Dependency> const &dependency);
};

class Launcher : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Launcher>>;
  explicit Launcher(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Launcher() {}

public:
  /**   */
  virtual std::shared_ptr<LauncherParameters> parameters();
  /**   */
  virtual std::string environment(std::string const &key);
  /** Sets an environment variable and returns the old value (if any)
  */
  virtual std::string env(std::string const &key, std::string const &value);
  /** Gets the value of the environment variable
  */
  virtual std::string env(std::string const &string);
  /**   */
  virtual void set_notification_url(std::string const &string);
  /** Sets the temporary directory for this launcher
  */
  virtual void set_tmpdir(std::shared_ptr<Path> const &path);
};

class Dependency : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Dependency>>;
  explicit Dependency(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Dependency() {}

public:
};

class SingleHostConnector : public Connector {
protected:
  friend struct RPCConverter<std::shared_ptr<SingleHostConnector>>;
  explicit SingleHostConnector(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  SingleHostConnector() {}

public:
};

class CommandComponent : public AbstractCommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<CommandComponent>>;
  explicit CommandComponent(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  CommandComponent() {}

public:
};

class ConnectorOptions : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<ConnectorOptions>>;
  explicit ConnectorOptions(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  ConnectorOptions() {}

public:
};

class LauncherParameters : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<LauncherParameters>>;
  explicit LauncherParameters(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  LauncherParameters() {}

public:
};

class Path : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Path>>;
  explicit Path(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  virtual std::string toString();
  /**   */
  virtual bool exists();
  /** Creates this folder, if it does not exist.  Also creates any ancestor
folders which do not exist.  This method does nothing if the folder
already exists.
  */
  virtual void mkdirs();
  /**   */
  static std::shared_ptr<Path> toPath(std::string const &path);
  /**   */
  virtual std::string read_all();
  /**   */
  virtual int64_t get_size();
  /** Get the file path, ignoring the file scheme
  */
  virtual std::string get_path();
  /**   */
  virtual std::string uri();
  /**   */
  virtual std::string toSource();
};

class JsonParameterFile : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<JsonParameterFile>>;
  explicit JsonParameterFile(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  JsonParameterFile(std::string const &key, std::string const &value);
};

class Commands : public AbstractCommand {
protected:
  friend struct RPCConverter<std::shared_ptr<Commands>>;
  explicit Commands(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  Commands();
  /**   */
  Commands(std::vector<std::shared_ptr<AbstractCommand>> const &abstractCommand);
  /**   */
  virtual void add(std::shared_ptr<AbstractCommand> const &abstractCommand);
};

class Namespace : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Namespace>>;
  explicit Namespace(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  Namespace(std::string const &string, optional<std::string> const &string_1 = optional<std::string>());
  /**   */
  virtual std::string uri();
};

class XPM : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<XPM>>;
  explicit XPM(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  XPM() {}

public:
  /**   */
  virtual bool simulate();
  /** Set the simulate flag: When true, the jobs are not submitted but just output
  */
  virtual bool simulate(bool const &boolean);
  /** com.sun.javafx.binding.StringConstant@a1cdc6d  */
  static std::shared_ptr<TokenResource> token(std::string const &path);
  /**   */
  virtual std::string ns();
  /** com.sun.javafx.binding.StringConstant@610f7aacom.sun.javafx.binding.StringConstant@6a03bcb1  */
  static std::shared_ptr<TokenResource> token_resource(std::string const &path, bool const &post_process);
  /** Retrieve (or creates) a token resource with a given xpath
com.sun.javafx.binding.StringConstant@21b2e768  */
  static std::shared_ptr<TokenResource> token_resource(std::string const &path);
  /** Sets the logger debug level
  */
  virtual void log_level(std::string const &name, std::string const &level);
};

class SubCommand : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<SubCommand>>;
  explicit SubCommand(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  SubCommand() {}

public:
};

class TokenResource : public Resource {
protected:
  friend struct RPCConverter<std::shared_ptr<TokenResource>>;
  explicit TokenResource(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  TokenResource() {}

public:
  /**   */
  virtual void set_limit(int32_t const &int_1);
  /**   */
  virtual int32_t getLimit();
  /**   */
  virtual int32_t used();
};

class CommandOutput : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<CommandOutput>>;
  explicit CommandOutput(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  CommandOutput() {}

public:
};

class CommandString : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<CommandString>>;
  explicit CommandString(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  CommandString(std::string const &string);
  /**   */
  virtual std::string toString();
};

class Pipe : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<Pipe>>;
  explicit Pipe(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Pipe() {}

public:
};

class CommandPath : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<CommandPath>>;
  explicit CommandPath(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  CommandPath(std::string const &pathname);
};

class OARParameters : public LauncherParameters {
protected:
  friend struct RPCConverter<std::shared_ptr<OARParameters>>;
  explicit OARParameters(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  OARParameters() {}

public:
};

class OARLauncher : public Launcher {
protected:
  friend struct RPCConverter<std::shared_ptr<OARLauncher>>;
  explicit OARLauncher(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  OARLauncher(std::shared_ptr<Connector> const &connector);
  /**   */
  virtual std::shared_ptr<OARParameters> oarParameters();
  /** Send a notification email. Process it to notify experimaestro when a job status changes.
  */
  virtual void email(std::string const &string);
  /**   */
  virtual void use_notify(bool const &boolean);
};

class LocalhostConnector : public SingleHostConnector {
protected:
  friend struct RPCConverter<std::shared_ptr<LocalhostConnector>>;
  explicit LocalhostConnector(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  LocalhostConnector();
  /**   */
  virtual std::string env(std::string const &string);
};

class Functions : public virtual ServerObject {
protected:
  friend struct RPCConverter<std::shared_ptr<Functions>>;
  explicit Functions(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

  Functions() {}

public:
  /** Returns a path object from an URI
  */
  static std::shared_ptr<Path> path(std::string const &uri);
  /**   */
  static std::shared_ptr<Path> path(std::shared_ptr<Path> const &uri);
  /** Defines a new relationship between a network share and a path on a connector
com.sun.javafx.binding.StringConstant@3f56875e  */
  static void define_share(std::string const &host, std::string const &share, std::shared_ptr<SingleHostConnector> const &connector, std::string const &path, optional<int32_t> const &priority = optional<int32_t>());
  /** Defines the default launcher
  */
  static void set_default_launcher(std::shared_ptr<Launcher> const &launcher);
  /** Set the experiment for all future commands
com.sun.javafx.binding.StringConstant@484970b0  */
  static void set_experiment(std::string const &identifier, optional<bool> const &holdPrevious = optional<bool>());
  /**   */
  static void set_workdir(std::shared_ptr<Path> const &path);
  /**   */
  static std::shared_ptr<LocalhostConnector> get_localhost_connector();
  /** Returns the notification URL
  */
  static std::string notification_url();
};

class ParameterFile : public CommandComponent {
protected:
  friend struct RPCConverter<std::shared_ptr<ParameterFile>>;
  explicit ParameterFile(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  ParameterFile(std::string const &key, std::string const &content);
};

class CommandLineTask : public Job {
protected:
  friend struct RPCConverter<std::shared_ptr<CommandLineTask>>;
  explicit CommandLineTask(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  CommandLineTask(std::shared_ptr<Path> const &path);
  /**   */
  virtual void command(std::shared_ptr<AbstractCommand> const &abstractCommand);
};

class ReadWriteDependency : public Dependency {
protected:
  friend struct RPCConverter<std::shared_ptr<ReadWriteDependency>>;
  explicit ReadWriteDependency(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  ReadWriteDependency(std::string const &locator);
  /**   */
  ReadWriteDependency(std::shared_ptr<Resource> const &resource);
};

class Command : public AbstractCommandComponent, public AbstractCommand {
protected:
  friend struct RPCConverter<std::shared_ptr<Command>>;
  explicit Command(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  Command();
  /**   */
  virtual void add(std::vector<std::shared_ptr<AbstractCommandComponent>> const &abstractCommandComponent);
  /**   */
  virtual void add(std::vector<std::string> const &string);
  /**   */
  virtual void add_subcommand(std::shared_ptr<Commands> const &commands);
};

class DirectLauncher : public Launcher {
protected:
  friend struct RPCConverter<std::shared_ptr<DirectLauncher>>;
  explicit DirectLauncher(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  DirectLauncher(std::shared_ptr<Connector> const &connector);
};

class SSHConnector : public SingleHostConnector {
protected:
  friend struct RPCConverter<std::shared_ptr<SSHConnector>>;
  explicit SSHConnector(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  SSHConnector(std::string const &string, std::string const &string_1, std::shared_ptr<ConnectorOptions> const &connectorOptions);
  /**   */
  virtual std::string env(std::shared_ptr<Launcher> const &launcher, optional<std::string> const &string = optional<std::string>());
};

class SSHOptions : public ConnectorOptions {
protected:
  friend struct RPCConverter<std::shared_ptr<SSHOptions>>;
  explicit SSHOptions(ObjectIdentifier o);
  virtual std::string const &__name__() const override;

public:
  /**   */
  SSHOptions();
  /**   */
  virtual std::string hostname();
  /**   */
  virtual void port(int32_t const &int_1);
  /**   */
  virtual std::shared_ptr<SSHOptions> check_host(bool const &boolean);
  /**   */
  virtual void set_use_ssh_agent(bool const &boolean);
  /**   */
  virtual void set_stream_proxy(std::string const &uri, std::shared_ptr<SSHOptions> const &options);
  /**   */
  virtual void set_stream_proxy(std::shared_ptr<SSHConnector> const &proxy);
  /**   */
  virtual void hostname(std::string const &string);
  /**   */
  virtual void username(std::string const &string);
  /**   */
  virtual void password(std::string const &string);
  /**   */
  virtual std::string username();
};

} }// xpm::rpc namespace
#endif
