## MogBnB Setup Guide
<p>
  In order to setup and use the distributed system of this MapReduce framework you must do the following:
</p>
<p>
  a) Edit the config file and use the proper ip addresses for all machines-nodes. (The same Config file can be used on all machines, if it is correct).
</p>
<p>
  b) Run at least one Worker, for each worker insert the worker's sequential number as the "id" as the only parameter (starting from 1..n).
</p>
<p>
  c) Run exactly one Master node (after running all workers first!), insert the total number of workers as the only parameter.
</p>
<p>
  c) Run exactly one Reducer node, insert the total number of workers as the only parameter.
</p>
