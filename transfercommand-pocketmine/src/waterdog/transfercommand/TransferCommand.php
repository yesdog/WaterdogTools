<?php

	 namespace waterdog\transfercommand;

	 use pocketmine\command\Command;
	 use pocketmine\command\CommandSender;
	 use pocketmine\Player;

	 class TransferCommand extends Command
	 {

		  public function __construct ()
		  {

				parent::__construct( "transfer", "Transfer a player through Messaging Channel" );
				$this->setPermission( "transfer.use" );
				$this->setPermissionMessage( "§cYou cannot access the Transfer Command!" );
				$this->setUsage( "/transfer <Player | Server > [Server]" );
		  }


		  public function execute ( CommandSender $sender, string $commandLabel, array $args )
		  {

				if ( $sender instanceof Player ) {
					 if ( count( $args ) == 0 ) {
						  // Invalid Syntax
						  $sender->sendMessage( "§cPlease use /transfer <Server-Name> or /transfer <Player> <Server-Name>" );
					 } else if ( count( $args ) == 1 ) {
						  // Transfer Executor
						  API::transfer( $sender, $args[ 0 ] );
					 } else {
						  // Transfer Other Player
						  $sender->sendMessage( "§aTransferring Player §c" . $args[ 0 ] . "§a to server §c" . $args[ 1 ] . "§a..." );
						  API::transferOther( $args[ 0 ], $args[ 1 ] );
					 }
				}
		  }
	 }
