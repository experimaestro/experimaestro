#include <xpm/rpc/objects.hpp>

namespace xpm {
namespace rpc{
std::string const &Resource::__name__() const { static std::string name = "Resource"; return name; }
Resource::Resource(ObjectIdentifier o) : ServerObject(o) {} 
std::string Resource::toString() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Resource.toString", params));
}

std::shared_ptr<Path> Resource::resolve(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__call__("objects.Resource.resolve", params));
}

std::shared_ptr<Path> Resource::file() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__call__("objects.Resource.file", params));
}

std::shared_ptr<Path> Resource::output() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__call__("objects.Resource.output", params));
}

AbstractCommandComponent::AbstractCommandComponent(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &Connector::__name__() const { static std::string name = "Connector"; return name; }
Connector::Connector(ObjectIdentifier o) : ServerObject(o) {} 
std::string Connector::resolve(std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  return RPCConverter<std::string>::toCPP(__call__("objects.Connector.resolve", params));
}

std::shared_ptr<Connector> Connector::create(std::string const &string, std::string const &string_1, std::shared_ptr<ConnectorOptions> const &connectorOptions) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  params["string_1"] = RPCConverter<std::string>::toJson(string_1);
  params["connectorOptions"] = RPCConverter<std::shared_ptr<ConnectorOptions>>::toJson(connectorOptions);
  return RPCConverter<std::shared_ptr<Connector>>::toCPP(__static_call__("objects.Connector.create", params));
}

std::shared_ptr<Launcher> Connector::default_launcher() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<Launcher>>::toCPP(__call__("objects.Connector.default_launcher", params));
}

std::string const &Job::__name__() const { static std::string name = "Job"; return name; }
Job::Job(ObjectIdentifier o) : ServerObject(o), Resource(o) {} 
std::string const &LauncherParameters::__name__() const { static std::string name = "LauncherParameters"; return name; }
LauncherParameters::LauncherParameters(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &Launcher::__name__() const { static std::string name = "Launcher"; return name; }
Launcher::Launcher(ObjectIdentifier o) : ServerObject(o) {} 
std::shared_ptr<LauncherParameters> Launcher::parameters() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<LauncherParameters>>::toCPP(__call__("objects.Launcher.parameters", params));
}

std::string Launcher::environment(std::string const &key) {
  nlohmann::json params = nlohmann::json::object();
  params["key"] = RPCConverter<std::string>::toJson(key);
  return RPCConverter<std::string>::toCPP(__call__("objects.Launcher.environment", params));
}

std::string Launcher::env(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  return RPCConverter<std::string>::toCPP(__call__("objects.Launcher.env", params));
}

std::string Launcher::env(std::string const &key, std::string const &value) {
  nlohmann::json params = nlohmann::json::object();
  params["key"] = RPCConverter<std::string>::toJson(key);
  params["value"] = RPCConverter<std::string>::toJson(value);
  return RPCConverter<std::string>::toCPP(__call__("objects.Launcher.env", params));
}

void Launcher::set_notification_url(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __call__("objects.Launcher.set_notification_url", params);
}

void Launcher::set_tmpdir(std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  __call__("objects.Launcher.set_tmpdir", params);
}

std::string const &SingleHostConnector::__name__() const { static std::string name = "SingleHostConnector"; return name; }
SingleHostConnector::SingleHostConnector(ObjectIdentifier o) : ServerObject(o), Connector(o) {} 
std::string const &Dependency::__name__() const { static std::string name = "Dependency"; return name; }
Dependency::Dependency(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &CommandComponent::__name__() const { static std::string name = "CommandComponent"; return name; }
CommandComponent::CommandComponent(ObjectIdentifier o) : ServerObject(o), AbstractCommandComponent(o) {} 
std::string const &AbstractCommand::__name__() const { static std::string name = "AbstractCommand"; return name; }
AbstractCommand::AbstractCommand(ObjectIdentifier o) : ServerObject(o) {} 
std::shared_ptr<CommandOutput> AbstractCommand::output() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<CommandOutput>>::toCPP(__call__("objects.AbstractCommand.output", params));
}

void AbstractCommand::add_dependency(std::shared_ptr<Dependency> const &dependency) {
  nlohmann::json params = nlohmann::json::object();
  params["dependency"] = RPCConverter<std::shared_ptr<Dependency>>::toJson(dependency);
  __call__("objects.AbstractCommand.add_dependency", params);
}

std::string const &ConnectorOptions::__name__() const { static std::string name = "ConnectorOptions"; return name; }
ConnectorOptions::ConnectorOptions(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &Path::__name__() const { static std::string name = "Path"; return name; }
Path::Path(ObjectIdentifier o) : ServerObject(o) {} 
std::string Path::toString() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Path.toString", params));
}

bool Path::exists() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<bool>::toCPP(__call__("objects.Path.exists", params));
}

void Path::mkdirs() {
  nlohmann::json params = nlohmann::json::object();
  __call__("objects.Path.mkdirs", params);
}

std::shared_ptr<Path> Path::toPath(std::string const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::string>::toJson(path);
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__static_call__("objects.Path.toPath", params));
}

std::string Path::read_all() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Path.read_all", params));
}

int64_t Path::get_size() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<int64_t>::toCPP(__call__("objects.Path.get_size", params));
}

std::string Path::get_path() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Path.get_path", params));
}

std::string Path::uri() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Path.uri", params));
}

std::string Path::toSource() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Path.toSource", params));
}

std::string const &OARParameters::__name__() const { static std::string name = "OARParameters"; return name; }
OARParameters::OARParameters(ObjectIdentifier o) : ServerObject(o), LauncherParameters(o) {} 
ParameterFile::ParameterFile(std::string const &key, std::string const &content) {
  nlohmann::json params = nlohmann::json::object();
  params["key"] = RPCConverter<std::string>::toJson(key);
  params["content"] = RPCConverter<std::string>::toJson(content);
  __set__(__static_call__("objects.ParameterFile.__init__", params));
}

std::string const &ParameterFile::__name__() const { static std::string name = "ParameterFile"; return name; }
ParameterFile::ParameterFile(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
std::string const &XPM::__name__() const { static std::string name = "XPM"; return name; }
XPM::XPM(ObjectIdentifier o) : ServerObject(o) {} 
std::shared_ptr<Path> XPM::file(std::string const &filepath) {
  nlohmann::json params = nlohmann::json::object();
  params["filepath"] = RPCConverter<std::string>::toJson(filepath);
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__call__("objects.XPM.file", params));
}

std::string XPM::ns() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.XPM.ns", params));
}

bool XPM::simulate() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<bool>::toCPP(__call__("objects.XPM.simulate", params));
}

bool XPM::simulate(bool const &boolean) {
  nlohmann::json params = nlohmann::json::object();
  params["boolean"] = RPCConverter<bool>::toJson(boolean);
  return RPCConverter<bool>::toCPP(__call__("objects.XPM.simulate", params));
}

std::shared_ptr<Task> XPM::get_task(std::string const &string, std::string const &string_1) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  params["string_1"] = RPCConverter<std::string>::toJson(string_1);
  return RPCConverter<std::shared_ptr<Task>>::toCPP(__call__("objects.XPM.get_task", params));
}

void XPM::publish() {
  nlohmann::json params = nlohmann::json::object();
  __call__("objects.XPM.publish", params);
}

std::shared_ptr<TokenResource> XPM::token_resource(std::string const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::string>::toJson(path);
  return RPCConverter<std::shared_ptr<TokenResource>>::toCPP(__static_call__("objects.XPM.token_resource", params));
}

std::shared_ptr<TokenResource> XPM::token_resource(std::string const &path, bool const &post_process) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::string>::toJson(path);
  params["post_process"] = RPCConverter<bool>::toJson(post_process);
  return RPCConverter<std::shared_ptr<TokenResource>>::toCPP(__static_call__("objects.XPM.token_resource", params));
}

void XPM::log_level(std::string const &name, std::string const &level) {
  nlohmann::json params = nlohmann::json::object();
  params["name"] = RPCConverter<std::string>::toJson(name);
  params["level"] = RPCConverter<std::string>::toJson(level);
  __call__("objects.XPM.log_level", params);
}

std::string XPM::get_script_path() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.XPM.get_script_path", params));
}

std::shared_ptr<TokenResource> XPM::token(std::string const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::string>::toJson(path);
  return RPCConverter<std::shared_ptr<TokenResource>>::toCPP(__static_call__("objects.XPM.token", params));
}

std::string const &Task::__name__() const { static std::string name = "Task"; return name; }
Task::Task(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &Functions::__name__() const { static std::string name = "Functions"; return name; }
Functions::Functions(ObjectIdentifier o) : ServerObject(o) {} 
std::shared_ptr<Path> Functions::path(std::shared_ptr<Path> const &uri) {
  nlohmann::json params = nlohmann::json::object();
  params["uri"] = RPCConverter<std::shared_ptr<Path>>::toJson(uri);
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__static_call__("objects.Functions.path", params));
}

std::shared_ptr<Path> Functions::path(std::string const &uri) {
  nlohmann::json params = nlohmann::json::object();
  params["uri"] = RPCConverter<std::string>::toJson(uri);
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__static_call__("objects.Functions.path", params));
}

std::shared_ptr<Path> Functions::script_file() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<Path>>::toCPP(__static_call__("objects.Functions.script_file", params));
}

void Functions::define_share(std::string const &host, std::string const &share, std::shared_ptr<SingleHostConnector> const &connector, std::string const &path, int32_t const &priority) {
  nlohmann::json params = nlohmann::json::object();
  params["host"] = RPCConverter<std::string>::toJson(host);
  params["share"] = RPCConverter<std::string>::toJson(share);
  params["connector"] = RPCConverter<std::shared_ptr<SingleHostConnector>>::toJson(connector);
  params["path"] = RPCConverter<std::string>::toJson(path);
  params["priority"] = RPCConverter<int32_t>::toJson(priority);
  __static_call__("objects.Functions.define_share", params);
}

void Functions::set_default_launcher(std::shared_ptr<Launcher> const &launcher) {
  nlohmann::json params = nlohmann::json::object();
  params["launcher"] = RPCConverter<std::shared_ptr<Launcher>>::toJson(launcher);
  __static_call__("objects.Functions.set_default_launcher", params);
}

void Functions::inspect_java_repository(std::shared_ptr<Connector> const &connector, std::vector<std::string> const &string, std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["connector"] = RPCConverter<std::shared_ptr<Connector>>::toJson(connector);
  params["string"] = RPCConverter<std::vector<std::string>>::toJson(string);
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  __static_call__("objects.Functions.inspect_java_repository", params);
}

void Functions::load_java_repository(std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  __static_call__("objects.Functions.load_java_repository", params);
}

void Functions::load_repository(std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  __static_call__("objects.Functions.load_repository", params);
}

void Functions::set_experiment(std::string const &identifier, bool const &holdPrevious) {
  nlohmann::json params = nlohmann::json::object();
  params["identifier"] = RPCConverter<std::string>::toJson(identifier);
  params["holdPrevious"] = RPCConverter<bool>::toJson(holdPrevious);
  __static_call__("objects.Functions.set_experiment", params);
}

void Functions::set_workdir(std::shared_ptr<Path> const &path) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  __static_call__("objects.Functions.set_workdir", params);
}

std::shared_ptr<LocalhostConnector> Functions::get_localhost_connector() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<LocalhostConnector>>::toCPP(__static_call__("objects.Functions.get_localhost_connector", params));
}

std::string Functions::notification_url() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__static_call__("objects.Functions.notification_url", params));
}

Namespace::Namespace(std::string const &string, std::string const &string_1) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  params["string_1"] = RPCConverter<std::string>::toJson(string_1);
  __set__(__static_call__("objects.Namespace.__init__", params));
}

std::string const &Namespace::__name__() const { static std::string name = "Namespace"; return name; }
Namespace::Namespace(ObjectIdentifier o) : ServerObject(o) {} 
std::string Namespace::uri() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.Namespace.uri", params));
}

std::string const &JsonTask::__name__() const { static std::string name = "JsonTask"; return name; }
JsonTask::JsonTask(ObjectIdentifier o) : ServerObject(o) {} 
std::shared_ptr<AbstractCommand> JsonTask::command() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<AbstractCommand>>::toCPP(__call__("objects.JsonTask.command", params));
}

std::string const &CommandOutput::__name__() const { static std::string name = "CommandOutput"; return name; }
CommandOutput::CommandOutput(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
std::string const &SSHConnector::__name__() const { static std::string name = "SSHConnector"; return name; }
SSHConnector::SSHConnector(ObjectIdentifier o) : ServerObject(o), SingleHostConnector(o) {} 
std::string SSHConnector::env(std::shared_ptr<Launcher> const &launcher, std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["launcher"] = RPCConverter<std::shared_ptr<Launcher>>::toJson(launcher);
  params["string"] = RPCConverter<std::string>::toJson(string);
  return RPCConverter<std::string>::toCPP(__call__("objects.SSHConnector.env", params));
}

ReadWriteDependency::ReadWriteDependency(std::string const &locator) {
  nlohmann::json params = nlohmann::json::object();
  params["locator"] = RPCConverter<std::string>::toJson(locator);
  __set__(__static_call__("objects.ReadWriteDependency.__init__", params));
}

ReadWriteDependency::ReadWriteDependency(std::shared_ptr<Resource> const &resource) {
  nlohmann::json params = nlohmann::json::object();
  params["resource"] = RPCConverter<std::shared_ptr<Resource>>::toJson(resource);
  __set__(__static_call__("objects.ReadWriteDependency.__init__", params));
}

std::string const &ReadWriteDependency::__name__() const { static std::string name = "ReadWriteDependency"; return name; }
ReadWriteDependency::ReadWriteDependency(ObjectIdentifier o) : ServerObject(o), Dependency(o) {} 
OARLauncher::OARLauncher(std::shared_ptr<Connector> const &connector) {
  nlohmann::json params = nlohmann::json::object();
  params["connector"] = RPCConverter<std::shared_ptr<Connector>>::toJson(connector);
  __set__(__static_call__("objects.OARLauncher.__init__", params));
}

std::string const &OARLauncher::__name__() const { static std::string name = "OARLauncher"; return name; }
OARLauncher::OARLauncher(ObjectIdentifier o) : ServerObject(o), Launcher(o) {} 
std::shared_ptr<OARParameters> OARLauncher::oarParameters() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::shared_ptr<OARParameters>>::toCPP(__call__("objects.OARLauncher.oarParameters", params));
}

void OARLauncher::email(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __call__("objects.OARLauncher.email", params);
}

void OARLauncher::use_notify(bool const &boolean) {
  nlohmann::json params = nlohmann::json::object();
  params["boolean"] = RPCConverter<bool>::toJson(boolean);
  __call__("objects.OARLauncher.use_notify", params);
}

CommandString::CommandString(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __set__(__static_call__("objects.CommandString.__init__", params));
}

std::string const &CommandString::__name__() const { static std::string name = "CommandString"; return name; }
CommandString::CommandString(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
std::string CommandString::toString() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.CommandString.toString", params));
}

CommandPath::CommandPath(std::string const &pathname) {
  nlohmann::json params = nlohmann::json::object();
  params["pathname"] = RPCConverter<std::string>::toJson(pathname);
  __set__(__static_call__("objects.CommandPath.__init__", params));
}

std::string const &CommandPath::__name__() const { static std::string name = "CommandPath"; return name; }
CommandPath::CommandPath(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
LocalhostConnector::LocalhostConnector() {
  nlohmann::json params = nlohmann::json::object();
  __set__(__static_call__("objects.LocalhostConnector.__init__", params));
}

std::string const &LocalhostConnector::__name__() const { static std::string name = "LocalhostConnector"; return name; }
LocalhostConnector::LocalhostConnector(ObjectIdentifier o) : ServerObject(o), SingleHostConnector(o) {} 
std::string LocalhostConnector::env(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  return RPCConverter<std::string>::toCPP(__call__("objects.LocalhostConnector.env", params));
}

DirectLauncher::DirectLauncher(std::shared_ptr<Connector> const &connector) {
  nlohmann::json params = nlohmann::json::object();
  params["connector"] = RPCConverter<std::shared_ptr<Connector>>::toJson(connector);
  __set__(__static_call__("objects.DirectLauncher.__init__", params));
}

std::string const &DirectLauncher::__name__() const { static std::string name = "DirectLauncher"; return name; }
DirectLauncher::DirectLauncher(ObjectIdentifier o) : ServerObject(o), Launcher(o) {} 
std::string const &JavaTaskFactory::__name__() const { static std::string name = "JavaTaskFactory"; return name; }
JavaTaskFactory::JavaTaskFactory(ObjectIdentifier o) : ServerObject(o) {} 
Commands::Commands() {
  nlohmann::json params = nlohmann::json::object();
  __set__(__static_call__("objects.Commands.__init__", params));
}

Commands::Commands(std::vector<std::shared_ptr<AbstractCommand>> const &abstractCommand) {
  nlohmann::json params = nlohmann::json::object();
  params["abstractCommand"] = RPCConverter<std::vector<std::shared_ptr<AbstractCommand>>>::toJson(abstractCommand);
  __set__(__static_call__("objects.Commands.__init__", params));
}

std::string const &Commands::__name__() const { static std::string name = "Commands"; return name; }
Commands::Commands(ObjectIdentifier o) : ServerObject(o), AbstractCommand(o) {} 
void Commands::add(std::shared_ptr<AbstractCommand> const &abstractCommand) {
  nlohmann::json params = nlohmann::json::object();
  params["abstractCommand"] = RPCConverter<std::shared_ptr<AbstractCommand>>::toJson(abstractCommand);
  __call__("objects.Commands.add", params);
}

std::string const &Pipe::__name__() const { static std::string name = "Pipe"; return name; }
Pipe::Pipe(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
std::string const &JsonParameterFile::__name__() const { static std::string name = "JsonParameterFile"; return name; }
JsonParameterFile::JsonParameterFile(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
std::string const &Module::__name__() const { static std::string name = "Module"; return name; }
Module::Module(ObjectIdentifier o) : ServerObject(o) {} 
std::string const &CommandLineTask::__name__() const { static std::string name = "CommandLineTask"; return name; }
CommandLineTask::CommandLineTask(ObjectIdentifier o) : ServerObject(o), Job(o) {} 
std::shared_ptr<Resource> CommandLineTask::submitJob(std::shared_ptr<Path> const &path, std::shared_ptr<AbstractCommand> const &abstractCommand) {
  nlohmann::json params = nlohmann::json::object();
  params["path"] = RPCConverter<std::shared_ptr<Path>>::toJson(path);
  params["abstractCommand"] = RPCConverter<std::shared_ptr<AbstractCommand>>::toJson(abstractCommand);
  return RPCConverter<std::shared_ptr<Resource>>::toCPP(__static_call__("objects.CommandLineTask.submitJob", params));
}

std::string const &TokenResource::__name__() const { static std::string name = "TokenResource"; return name; }
TokenResource::TokenResource(ObjectIdentifier o) : ServerObject(o), Resource(o) {} 
int32_t TokenResource::used() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<int32_t>::toCPP(__call__("objects.TokenResource.used", params));
}

void TokenResource::set_limit(int32_t const &int_1) {
  nlohmann::json params = nlohmann::json::object();
  params["int_1"] = RPCConverter<int32_t>::toJson(int_1);
  __call__("objects.TokenResource.set_limit", params);
}

int32_t TokenResource::getLimit() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<int32_t>::toCPP(__call__("objects.TokenResource.getLimit", params));
}

SSHOptions::SSHOptions() {
  nlohmann::json params = nlohmann::json::object();
  __set__(__static_call__("objects.SSHOptions.__init__", params));
}

std::string const &SSHOptions::__name__() const { static std::string name = "SSHOptions"; return name; }
SSHOptions::SSHOptions(ObjectIdentifier o) : ServerObject(o), ConnectorOptions(o) {} 
std::string SSHOptions::hostname() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.SSHOptions.hostname", params));
}

std::shared_ptr<SSHOptions> SSHOptions::check_host(bool const &boolean) {
  nlohmann::json params = nlohmann::json::object();
  params["boolean"] = RPCConverter<bool>::toJson(boolean);
  return RPCConverter<std::shared_ptr<SSHOptions>>::toCPP(__call__("objects.SSHOptions.check_host", params));
}

void SSHOptions::set_use_ssh_agent(bool const &boolean) {
  nlohmann::json params = nlohmann::json::object();
  params["boolean"] = RPCConverter<bool>::toJson(boolean);
  __call__("objects.SSHOptions.set_use_ssh_agent", params);
}

void SSHOptions::set_stream_proxy(std::string const &uri, std::shared_ptr<SSHOptions> const &options) {
  nlohmann::json params = nlohmann::json::object();
  params["uri"] = RPCConverter<std::string>::toJson(uri);
  params["options"] = RPCConverter<std::shared_ptr<SSHOptions>>::toJson(options);
  __call__("objects.SSHOptions.set_stream_proxy", params);
}

void SSHOptions::set_stream_proxy(std::shared_ptr<SSHConnector> const &proxy) {
  nlohmann::json params = nlohmann::json::object();
  params["proxy"] = RPCConverter<std::shared_ptr<SSHConnector>>::toJson(proxy);
  __call__("objects.SSHOptions.set_stream_proxy", params);
}

void SSHOptions::hostname(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __call__("objects.SSHOptions.hostname", params);
}

void SSHOptions::port(int32_t const &int_1) {
  nlohmann::json params = nlohmann::json::object();
  params["int_1"] = RPCConverter<int32_t>::toJson(int_1);
  __call__("objects.SSHOptions.port", params);
}

void SSHOptions::username(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __call__("objects.SSHOptions.username", params);
}

void SSHOptions::password(std::string const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::string>::toJson(string);
  __call__("objects.SSHOptions.password", params);
}

std::string SSHOptions::username() {
  nlohmann::json params = nlohmann::json::object();
  return RPCConverter<std::string>::toCPP(__call__("objects.SSHOptions.username", params));
}

Command::Command() {
  nlohmann::json params = nlohmann::json::object();
  __set__(__static_call__("objects.Command.__init__", params));
}

std::string const &Command::__name__() const { static std::string name = "Command"; return name; }
Command::Command(ObjectIdentifier o) : ServerObject(o), AbstractCommandComponent(o) {} 
void Command::add(std::vector<std::string> const &string) {
  nlohmann::json params = nlohmann::json::object();
  params["string"] = RPCConverter<std::vector<std::string>>::toJson(string);
  __call__("objects.Command.add", params);
}

void Command::add(std::vector<std::shared_ptr<AbstractCommandComponent>> const &abstractCommandComponent) {
  nlohmann::json params = nlohmann::json::object();
  params["abstractCommandComponent"] = RPCConverter<std::vector<std::shared_ptr<AbstractCommandComponent>>>::toJson(abstractCommandComponent);
  __call__("objects.Command.add", params);
}

void Command::add_subcommand(std::shared_ptr<Commands> const &commands) {
  nlohmann::json params = nlohmann::json::object();
  params["commands"] = RPCConverter<std::shared_ptr<Commands>>::toJson(commands);
  __call__("objects.Command.add_subcommand", params);
}

std::string const &SubCommand::__name__() const { static std::string name = "SubCommand"; return name; }
SubCommand::SubCommand(ObjectIdentifier o) : ServerObject(o), CommandComponent(o) {} 
}} // namespace xpm::rpc