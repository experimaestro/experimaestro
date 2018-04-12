experimaestro
=============

A [full documentation is available](http://bpiwowar.github.io/experimaestro/)

Experimaestro is an experiment manager based on a server that contains a job scheduler (job dependencies, locking mechanisms) and a framework to write the experiments.

- A **job scheduler** that handles dependencies between jobs and provides locking mechanisms
   The job scheduler can be controlled via command line (`experimaestro` script) or via the web (where
   you can easily monitor jobs in real time)

- A **modular experiment description framework**, that allows easy description of the various parts of experiments:
    - Experiments are written in any language (currently supported: Python)
    - Tasks describe the components that can be used, take as input json objects and produce json objets as output
    - Tasks can be composed through the definition of an experimental plan

Both modules can be used independently even though they were designed to work together.

Experimaestro is in a **beta** state - which means that you might experience some bugs
while using it; but as I use it on a daily basis, their number and importance is
going down each day.


![A screenshot of experimaestro running](docs/xpm-screenshot.png)
