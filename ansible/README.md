Ansible - Project configuration
===============================

# Playbooks

For every Vagrant + Ansible ready project, we expect 4 playbooks to exist, namely:

* [default.yml](playbooks/default.yml)
* [demo.yml](playbooks/demo.yml)
* [production.yml](playbooks/production.yml)
* [test.yml](playbooks/test.yml)

See the comments in the top of the specific files for their purposes.

# Roles

Roles can be added as subfolders to the `roles/` folder. If a role common roles
evolve over time, they should be refactored out, and included using git-submodules.

See the [vim role submodule](https://github.com/magenta-aps/ansible-role-vim) for details.
