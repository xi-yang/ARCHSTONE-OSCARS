#!/bin/sh 
# usage list <number requested> <offset> <loginId> <institution> <attributes...>
#  -Djavax.net.debug=all will dump the ssl messages
test -d target/tmp || . bin/expandOneJar.sh
. bin/setclasspath.sh
java net.es.oscars.coord.coordServerTest -c list  $* 
