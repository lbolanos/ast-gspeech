<?php
//$fd3 = fopen('php://fd/3', 'r');

function getParameter( &$param, $name, $line ) {
	$arg2="agi_" . $name;
	$pos = strpos( $line, $arg2 );
	if( $pos !== FALSE ) {
		$param = substr($line, $pos + strlen($arg2) + 2 );
	}
}

function getB64AudioSample() {
	//global $fd3;
	//$buff = stream_get_contents($fd3);
	$buff = pack ("c*", 1, 2, 3);
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
echo "server:$serverUrl";
$fp = stream_socket_client($serverUrl, $errno, $errstr);
if (!$fp) {
	fwrite( STDERR, "ERROR: $errno - $errstr\n");
} else {
	foreach ($lines as $line ) {
		echo $line;
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
