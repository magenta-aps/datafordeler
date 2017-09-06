Vagrant-Ansible Example
=======================

Example project for running Ansible, inside a newly created virtual machine.

## Installation:
### Ubuntu 16.04 (Xenial)

To install vagrant, run:

    apt-get update
    apt-get install vagrant

To install Ansible, run:

    apt-get update
    apt-get install ansible

### Other platforms

Not supported, please figure out a solution yourself, and add it via. a pull
request.


## Usage:

Simply run `vagrant up`, and wait for the machine to be available.

After this, the machine can be accessed over ssh, using:

    vagrant ssh


## FAQ:
### What if I don't like virtualbox?

Fortunately several other providers can be utilized, specifically:
* `lxc`
* `libvirt`

Some provider specific setup is required however. The approach below is
confirmed to work on Ubuntu 16.04 (Xenial).

##### LXC

Install the system dependencies, using:

    apt-get update
    apt-get install lxc lxc-templates cgroup-lite redir

Install the vagrant lxc plugin, using:

    vagrant plugin install vagrant-lxc

The expected output is:

    Installing the 'vagrant-lxc' plugin. This can take a few minutes...
    Installed the plugin 'vagrant-lxc (1.2.3)'!

If the expected result is different, please check the Quirks section below.

##### Libvirt

Install the system dependencies, using:

    apt-get update
    apt-get build-dep vagrant ruby-libvirt
    apt-get install qemu libvirt-bin ebtables dnsmasq
    apt-get install libxslt-dev libxml2-dev libvirt-dev zlib1g-dev ruby-dev

Install the vagrant lxc plugin, using:

    vagrant plugin install vagrant-libvirt

The expected output is:

    Installing the 'vagrant-libvirt' plugin. This can take a few minutes...
    Installed the plugin 'vagrant-libvirt (0.0.40)'!

If the expected result is different, please check the Quirks section below.


## Quirks:
### Installing non-virtualbox provider fails.

It is a known issue, that installing the libvirt or LXC proviers, can result in
an issue, alike the one below:

    Installing the 'vagrant-lxc' plugin. This can take a few minutes...
    /usr/lib/ruby/2.3.0/rubygems/specification.rb:946:in `all=':
        undefined method `group_by' for nil:NilClass (NoMethodError)
    ...

The solution to this issue, is running code of the internet:

    sed -i'' "s/Specification.all = nil/Specification.reset/" \
        /usr/lib/ruby/vendor_ruby/vagrant/bundler.rb


