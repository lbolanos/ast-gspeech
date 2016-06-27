#!/usr/bin/perl

use MIME::Base64;
use IO::Socket::INET;
use IO::Handle;
use Time::HiRes qw[gettimeofday];


my $AUDIO_FD = 3;    # Audio is delivered on file descriptor 3
my $audio_fh = new IO::Handle;
$audio_fh->fdopen( $AUDIO_FD, "r" );

# flush after every write
$| = 1;

my ($socket,$client_socket);

sub getParameter{
	my ($param, $name, $line) = @_;
	my $arg2 = "agi_" . $name;
	my $pos = index( $line, $arg2 );
	if( $pos >= 0 ) {
		@_[0] = substr($line, $pos + length($arg2) + 2 );
	}   
}

sub getB64AudioSample {
	#my $u = "hola" ;	
	#$encoded = encode_base64($u);
	#return $encoded;
	#global $fd3;
	my $buffer;
	my $then = gettimeofday();
	#print STDERR "fde3=$fd3";
	my $bytes_read = $audio_fh->read( $buffer, 3200 );
	#$buffer = fgets( $fd3, 3200 );
	#$buffer = fread( $fd3, 3200 );
	#$buffer = dio_read ( $fd3, 3200 );
	$elapsed = gettimeofday() - $then;
	print STDERR "size=$bytes_read elapsed:$elapsed";
	#$bytes_read = pack ("c*", 1, 2, 3);
	return encode_base64( $buffer  );
}

my $script;
my $serverUrl;
@coins = ();

while( <STDIN> ){
	last if $_  eq "\n";
	push(@coins, $_);
	getParameter( $script, "arg_1", $_ );
	getParameter( $serverUrl, "arg_2", $_ );
}
push(@coins, "agi_network_script: $script\n" );
print STDERR $script;
print STDERR $serverUrl;

@parsedUrl = split(/:\/\/|:/, $serverUrl );
print STDERR join(", ", @parsedUrl);

# creating object interface of IO::Socket::INET modules which internally creates 
# socket, binds and connects to the TCP server running on the specific port.
$socket = new IO::Socket::INET (
	PeerHost => $parsedUrl[1],
	PeerPort => $parsedUrl[2],
	Proto => $parsedUrl[0],
) or die "ERROR in Socket Creation : $!\n";

print STDERR "TCP Connection Success.\n";

foreach ( @coins ) {
	print STDERR "=>" . $_;
#	print SOCKET $_;
	$socket->send( $_ );
}

#print STDERR "=>" . "\n";
#$socket->send( "\n" );

my $line;
print STDERR "Listening\n";	
while ($line = <$socket>) {	
	$pos = index( $line, "GETAUDIOSAMPLE" );
	if( $pos >= 0 ) {
		$buff = getB64AudioSample( ) . "\n";
		$socket->send( $buff );
		print STDERR "<=" . $line . "=>" . $buff;
	} else {
		print STDOUT $line;
		$f = <STDIN>;
		$socket->send( $f );
		print STDERR "<=" . $line . "=>" . $f;
	}    
}

sleep (10);
$socket->close();
$audio_fh->close();


