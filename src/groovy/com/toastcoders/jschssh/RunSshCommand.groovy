package com.toastcoders.jschssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.apache.log4j.Logger


/**
 * Created with IntelliJ IDEA.
 * User: Michael Rice
 * Twitter: @errr_
 * Website: http://www.errr-online.com/
 * Github: https://github.com/michaelrice
 * Date: 5/11/2014
 * Time: 4:00 PM
 * Licenses: MIT http://opensource.org/licenses/MIT
 */
class RunSshCommand extends ConnectionInfo {

    /**
     * Logger
     */
    private Logger log = Logger.getLogger(RunSshCommand)

    /**
     * The command you wish to run on the remote server.
     *
     */
    public String command

    /**
     * This provides a builder style method to execute a command on
     * a remote server using the ssh protocol.
     *
     * @param closure
     * @return String with the output of the command that ran.
     */
    public String execute(Closure closure) {
        run closure
        execute()
    }

    /**
     * This provides a method to execute a command on a remote host
     * using the ssh protocol.
     *
     * @param command
     * @return
     */
    public String execute() throws JSchException {
        try {
            Session session = fetchSession()
            log.trace("running command.")
            // Open channel to run command.
            ChannelExec channel = session.openChannel("exec")
            channel.setCommand(command)
            log.trace("set command on channel")
            channel.setInputStream(null)
            // TODO is there a better way to do this in groovy?
            // Right now its just copied from the jsch java examples.
            StringBuilder response = new StringBuilder()
            InputStream inputStream = channel.inputStream
            InputStream errorStream = channel.errStream
            channel.connect()
            int x = 0
            // Wait for the channel to close aka command to finish running.
            while (!channel.closed) {
                sleep(10)
                log.trace("Waiting 10 miliseconds for command to finish executing. ${x+1}")
                x++
                // "esxcli --formatter=xml hardware pci list"
                // got stuck and would never show closed
                if (x >= 30) {
                    // even after this it still showed -1 for exit status
                    break
                }
            }
            // If we get a non 0 exit status we need to read from the
            // error stream to return the user what the server said.
            if (channel.exitStatus > 0) {
                log.trace("Getting error stream. ${channel.exitStatus}")
                response = parseStream(errorStream, response, channel)
            }
            else {
                log.trace("Getting input stream. ${channel.exitStatus}")
                response = parseStream(inputStream, response, channel)
            }

            channel.disconnect()
            session.disconnect()
            log.debug("Successfully ran command on remote server. ${command}")
            return response.toString()
        }
        catch (JSchException e) {
            log.error("Error trying to execute command.",e)
            throw e
        }
        catch (Exception e1) {
            log.fatal("An unexpected Exception has happened.", e1)
            throw e1
        }
        return null
    }

    private StringBuilder parseStream(InputStream inputStream, StringBuilder response, ChannelExec channel) {
        byte[] tmp = new byte[1024]
        log.trace("Parsing Command stream to generate user output.")
        while (true) {
            // There is something in this stream to read
            while (inputStream.available() > 0) {
                int i = inputStream.read(tmp, 0, 1024)
                if (i < 0) {
                    break
                }
                // Append to our response.
                response.append(new String(tmp,0,i))
            }
            if (channel.isClosed()) {
                if (inputStream.available() > 0) {
                    continue
                }
                log.trace("exit-status: " + channel.getExitStatus())
                break
            }
        }
        return response
    }
}
