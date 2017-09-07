# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  # TODO: Ubuntu Xenial image
  config.vm.box = "debian/jessie64"
  config.vm.box_version = "8.7.0"
  config.vm.network :public_network,
      :dev => "virbr0",
      :mode => "bridge",
      :type => "bridge"

  vagrant_root = File.dirname(__FILE__)
  ENV['ANSIBLE_ROLES_PATH'] = "#{vagrant_root}/ansible/roles"

  config.vm.provision :ansible do |ansible|
    if ENV['PLAYBOOK']
        ansible.playbook = "ansible/playbooks/" + ENV['PLAYBOOK']
    else
        ansible.playbook = "ansible/playbooks/default.yml"
    end
    ansible.verbose = "vv"
  end
end
