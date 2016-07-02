#!/usr/bin/php
<?php
$fd3 = fopen('php://fd/3', 'r');
//$fd3 = dio_open ( "/proc/self/fd/3",O_RDONLY );

function getParameter( &$param, $name, $line ) {
	$arg2="agi_" . $name;
	$pos = strpos( $line, $arg2 );
	if( $pos !== FALSE ) {
		$param = substr($line, $pos + strlen($arg2) + 2 );
	}
}

function getB64AudioSample() {
	global $fd3;
	$then = microtime(true);
	fwrite(STDERR, "fde3=$fd3 size=$length elapsed:$elapsed" );
	//$buff = fgets( $fd3, 3200 );
	$buff = fread( $fd3, 3200 );
	//$buff = dio_read ( $fd3, 3200 );
	$elapsed = microtime(true) - $then;
	$length = strlen($buff);
	fwrite(STDERR, "size=$length elapsed:$elapsed" );
	//$buff = pack ("c*", 1, 2, 3);
	return base64_encode( $buff  );
}

$serverUrl=null;
$script=null;
$lines=array();
while($f = fgets(STDIN)){
	if ( $f  === "\n" ) {
		break;
	}
	$lines[]=$f;
	getParameter( $script, "arg_1", $f );
	getParameter( $serverUrl, "arg_2", $f );
}
$lines[] = "agi_network_script: $script\n";
fwrite(STDERR, "server:$serverUrl" );

fwrite(STDOUT, "STREAM FILE beep \"#\"\n");
$result = fgets(STDIN);


$fp = stream_socket_client($serverUrl, $errno, $errstr);
if (!$fp) {
	fwrite( STDERR, "ERROR: $errno - $errstr\n");
} else {
	foreach ($lines as $line ) {
		fwrite(STDERR, $line );
		fwrite($fp, $line );
	}
	//fwrite($fp, "\n");
	while (!feof($fp)) {
		$line = fgets($fp);
		$pos = strpos( $line, "GETAUDIOSAMPLE" );
		if( $pos !== FALSE ) {
			$buff = getB64AudioSample( ) . "\n";
			fwrite($fp, $buff );
			fwrite(STDERR, "<=" . $line . "=>" . $buff);
		} else {
			fwrite(STDOUT, $line);
			$f = fgets(STDIN);
			fwrite($fp, $f);
			fwrite(STDERR, "<=" . $line . "=>" . $f);
		}
	}
}

fclose($fp);
