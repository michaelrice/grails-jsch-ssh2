jsch-ssh2
=========

Grails Plugin using the jsch lib. This plugin also provides several useful builders to ease usage.


### Documentation

The full documentation can be found at: https://michaelrice.github.io/grails-jsch-ssh2/

### Usage

#### RunSshCommand Builder Usage

This plugin comes with a builder. From a controller in your grails app do the following

```groovy
import com.toastcoders.jschssh.RunSshCommand

class TestController {

    def index() {

        render new RunSshCommand().execute() {
            host = "10.12.254.10"
            username = "root"
            password = "password"
            command = "vmkping -X -c 1 10.12.254.1"
            strictHostKeyChecking = "yes"
        }
    }
}
```

If this were a VMWare ESXi 5.x HostSystem you would see output like the following:

```xml
<?xml version="1.0"?>
<esxcli:output xmlns:esxcli="http://www.vmware.com/Products/ESX/5.0/esxcli/">
    <esxcli:list type="struct">
        <esxcli:struct type="VMKPing">
            <esxcli:field name="Trace">
                <esxcli:list type="struct">
                    <esxcli:struct type="VMKPingTrace">
                        <esxcli:field name="Recieved Bytes"><esxcli:int>64</esxcli:int></esxcli:field>
                        <esxcli:field name="Host"><esxcli:string>10.12.254.1</esxcli:string></esxcli:field>
                        <esxcli:field name="ICMP Seq"><esxcli:int>0</esxcli:int></esxcli:field>
                        <esxcli:field name="TTL"><esxcli:int>64</esxcli:int></esxcli:field>
                        <esxcli:field name="Round-trip Time"><esxcli:int>2538</esxcli:int></esxcli:field>
                        <esxcli:field name="Dup"><esxcli:bool>false</esxcli:bool></esxcli:field>
                    </esxcli:struct>
                </esxcli:list>
            </esxcli:field>
            <esxcli:field name="Summary">
                <esxcli:struct type="VMKPingSummary">
                    <esxcli:field name="Host Addr"><esxcli:string>10.12.254.1</esxcli:string></esxcli:field>
                    <esxcli:field name="Transmitted"><esxcli:int>1</esxcli:int></esxcli:field>
                    <esxcli:field name="Recieved"><esxcli:int>1</esxcli:int></esxcli:field>
                    <esxcli:field name="Duplicated"><esxcli:int>0</esxcli:int></esxcli:field>
                    <esxcli:field name="Packet Lost"><esxcli:int>0</esxcli:int></esxcli:field>
                    <esxcli:field name="Round-trip Min"><esxcli:int>2537</esxcli:int></esxcli:field>
                    <esxcli:field name="Round-trip Avg"><esxcli:int>2537</esxcli:int></esxcli:field>
                    <esxcli:field name="Round-trip Max"><esxcli:int>2537</esxcli:int></esxcli:field>
                </esxcli:struct>
            </esxcli:field>
        </esxcli:struct>
    </esxcli:list>
</esxcli:output>
```

#### ScpFileTo Builder Usage

From a controller in your grails app do the following:

```groovy
import com.jcraft.jsch.JSchException
import com.toastcoders.jschssh.RunSshCommand
import com.toastcoders.jschssh.ScpFileTo


class TestController {

    def index() {

        try {
            new ScpFileTo().execute() {
                host = "10.12.254.10"
                username = "root"
                password = "password"
                localFile = "C:/users/errr/desktop/hello_world.txt"
                remoteFile = "/tmp/hello_world.txt"
                strictHostKeyChecking = "yes"
            }
        }
        catch (JSchException e) {
            log.trace("Oh noes!!", e)
        }

        render new RunSshCommand().execute() {
            host = "10.12.254.10"
            username = "root"
            password = "password"
            command = "cat /tmp/hello_world.txt"
            strictHostKeyChecking = "yes"
        }
    }
}
```

This would cause the contents of the "hello_world.txt" file to get printed to the screen. You would also have a file created in ```/tmp``` named ```hello_world.txt```


### TODO

Need to implement scp fetch and sftp put & fetch
