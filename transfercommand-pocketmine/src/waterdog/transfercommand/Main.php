<?php

	 namespace waterdog\transfercommand;

	 use pocketmine\plugin\PluginBase;

	 class Main extends PluginBase
	 {

		  public function onEnable ()
		  {

				$this->getServer()->getCommandMap()->register( $this->getName(), new TransferCommand );
				$this->getLogger()->info( "Plugin Enabled!" );
		  }
	 }
