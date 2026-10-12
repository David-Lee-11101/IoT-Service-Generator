# Legacy local process experiment

The original research tree included a disabled, host-specific integration experiment
that generated services, launched local Java processes, polled health endpoints, and
measured aggregate working-set memory. It depended on one private Windows workspace,
one local Maven installation, external database state, and operating-system process
utilities. It was excluded from automated tests and could wait without a deadline.

The executable experiment was removed from the public candidate tree because its
embedded host paths exposed private environment details and its process lifecycle was
not portable or bounded. The original Git commit remains the provenance record. The
replacement pipeline lives in SmartHome Integrated Logic Management and uses a fresh
task workspace, argument-array subprocesses, bounded startup and shutdown, an assigned
loopback port, and a deterministic local catalog.

No historical measurement from the removed experiment is presented as a newly
reproduced result.
