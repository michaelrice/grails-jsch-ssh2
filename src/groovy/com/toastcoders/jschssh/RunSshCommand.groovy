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
            Session session = this.getSession()
            log.trace("running command.")
            // Open channel to run command.
            ChannelExec channel = session.openChannel("exec")
            channel.setCommand(command)
            log.trace("set command on channel")
            // Im not 100% sure what this does. I used the example code from jsch site.
            channel.setInputStream(null)
            channel.setErrStream(System.err)
            InputStream inputStream
            channel.connect()

            // TODO is there a better way to do this in groovy?
            // Right now its just copied from the jsch java examples.
            byte[] tmp = new byte[1024]
            StringBuilder response = new StringBuilder()

            // If we get a non 0 exit status we need to read from the
            // error stream to return the user what the server said.
            if (channel.exitStatus != 0) {
                log.trace("Getting error stream.")
                inputStream = channel.getErrStream()
            }
            else {
                inputStream = channel.getInputStream()
            }

            while (true) {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024)
                    if (i < 0) {
                        break
                    }
                    response.append(new String(tmp,0,i))
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue
                    }
                    log.trace("exit-status: " + channel.getExitStatus())
                    break;
                }
                try {
                    Thread.sleep(1000)
                }
                catch (Exception ee) {
                }
            }
            channel.disconnect()
            session.disconnect()
            log.debug("Successfully ran command on remote server.")
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

    /**
     * Runs a passed closure to implement builder-style operation.
     *
     * @param closure
     */
    private void run(Closure closure) {
        closure.delegate = this
        closure.resolveStrategy = Closure.OWNER_FIRST
        closure.call()
    }
}