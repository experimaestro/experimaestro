/**
 * An experimental workspace
 */

#ifndef XPM_WORKSPACE_H
#define XPM_WORKSPACE_H

#include <string>
#include <set>
#include <queue>
#include <unordered_map>
#include <vector>
#include <mutex>

#include <xpm/filesystem.hpp>

namespace xpm {

class Launcher;
class Resource;
class Workspace;
class CommandLine;
struct JobPriorityComparator;

/**
 * A dependency between resources
 */
class Dependency {
public:
  virtual ~Dependency();

  /// Sets the dependent
  void target(ptr<Resource> const & resource);

  /// Is the dependency satisfied?
  virtual bool satisfied() = 0;

  /// Check the status and update the dependent if needed
  void check();

private:
  // The dependent
  ptr<Resource> _target;

  /// Old satisfaction status
  bool _oldSatisfied;
  
  /// Resource mutex
  std::mutex _mutex;

  friend class Resource;
};



/// Base class for any resource
class Resource 
#ifndef SWIG
: public std::enable_shared_from_this<Resource>
#endif
{
public:
  Resource();
  ~Resource();


  void addDependent(ptr<Dependency> const & dependency);
protected:
  /// Resource that depend on this one to be completed
  std::set<ptr<Dependency>> _dependents;

  /// Resource mutex
  std::mutex _mutex;

  /// Signals a change in a dependency
  virtual void dependencyChanged(Dependency & dependency, bool satisfied);

  friend class Dependency;
};




/// Tokens are used to limit the number of running jobs
class Token : public Resource {
};


/// A simple token based on counts
class CounterToken : public Token {
public:
  typedef uint32_t TokenCounter;

  /// Initialize the token
  CounterToken(TokenCounter limit);
  
  /// Set the limit
  void limit(TokenCounter _limit);

  /// Create a new dependency
  ptr<Dependency> createDependency(TokenCounter count);
private:
    /**
     * Maximum number of tokens available
     */
    TokenCounter _limit;

    /**
     * Number of used tokens
     */
    TokenCounter _usedTokens;
};

/**
 * Resource state
 * 
 * Possible paths:
 * - WAITING <-> READY -> RUNNING -> { ERROR, DONE } -> { WAITING, READY }
 * - {WAITING, READY} -> ON_HOLD -> READY
 */
enum struct JobState {
   /**
     * For a job only: the job is waiting dependencies status be met
     */
    WAITING,

    /**
     * For a job only: the job is waiting for an available thread status launch it
     */
    READY,

    /**
     * For a job only: The job is currently running
     */
    RUNNING,

    /**
     * The job is on hold
     */
    ON_HOLD,

    /**
     * The job ran but did not complete or the data was not generated
     */
    ERROR,

    /**
     * Completed (for a job) or generated (for a data resource)
     */
    DONE
};

/// Base class for jobs
class Job : public Resource {
public:
  Job(Path const & locator, ptr<Launcher> const & launcher);

  /// Adds a new dependency
  void addDependency(ptr<Dependency> const & dependency);

  /// The locator
  Path const & locator() { return _locator; }

  /// Returns true if the job is ready to run
  void ready();

  /// Get the current state
  JobState state() const;

  /// Run the job
  virtual void run() = 0;

  /// Get a dependency to this resource
  ptr<Dependency> dependency();

protected:
  friend class Workspace;
  friend struct JobPriorityComparator;

  /// Signals a dependency change
  virtual dependencyChanged(Dependency & dependency, bool satisfied) override;

  /// The workspace
  ptr<Workspace> _workspace;

  /// Main identifier of the task
  Path _locator;

  /// The launcher used for this task
  ptr<Launcher> _launcher;

  /// The dependencies of this task
  std::vector<ptr<Dependency>> _dependencies;

  /// Submission time
  std::time_t _submissionTime;

  /// Number of dependencies that are not satisifed
  size_t _unsatisfied;

  /// Resource state
  JobState _state;
};

/// A command line job
class CommandLineJob : public Job {
public:
  CommandLineJob(Path const & locator, 
    ptr<Launcher> const & launcher,
    ptr<CommandLine> const & command);
  
  virtual void run();
private:
  ptr<CommandLine> _command;
};

/// Defines the priority between two jobs
struct JobPriorityComparator {
  bool operator()( ptr<Job> const & lhs, ptr<Job> const & rhs ) const;

};

/** 
 * Workspace tracking resources, jobs and scheduling
 */
class Workspace 
#ifndef SWIG
: public std::enable_shared_from_this<Workspace>
#endif
{
public:
  /// Creates a new work space with a given path
  Workspace(std::string const &path);

  /// Submit a job
  void submit(ptr<Job> const & job);

private:
  /// All the jobs
  std::unordered_map<Path, ptr<Job>> _jobs;

  /// State mutex
  std::mutex _mutex;
};

}

#endif