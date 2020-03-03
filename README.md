Datafordeler
============

Meta repository for the datafordeler project.

Also main repository for documentation of the project.
For specific documentation about the documentation, see [This README](docs/README.md)

## Usage:
To clone this repository recursively, please run:

     git clone --recursive git@github.com:magenta-aps/datafordeler.git

Or using older versions of git:

     git clone git@github.com:magenta-aps/datafordeler.git
     cd datafordeler
     git submodule update --init --recursive
     
On Windows (inside `git-on-windows`), the clone command may have to become:

     ssh-agent bash -c 'ssh-add; git clone git@github.com:magenta-aps/datafordeler.git'

## Vagrant:
For information on the Vagrantfile, and Ansible, please check:
[This repository](https://github.com/magenta-aps/vagrant-ansible-example)

*Note: When using vagrant with Virtualbox, you'll be prompted to choose a 
bridged network interface. Select the interface that is being used to connect
to the internet.*


## Generate documentation

Use the vagrant image:
box_image=(ENV['BOX_NAME'] || 'ubuntu/bionic64')
box_version=(ENV['BOX_VERSION'] || '>=0')

