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
 * Date: 5/17/2014
 * Time: 17:41 PM
 * Licenses: MIT http://opensource.org/licenses/MIT
 */
class ScpFileTo extends ConnectionInfo {

    /**
     * Logger instance.
     */
    private Logger log = Logger.getLogger(ScpFileTo)

    /**
     * FileInputStream of the local file that will be getting sent to the
     * remote server.
     *
     */
    FileInputStream fileInputStream = null

    /**
     * Full path to the local file. The tomcat user must have read
     * access to this file.
     *
     */
    String localFile

    /**
     * Full path
     *
     */
    String remoteFile

    /**
     * Preserve timestamps from the file.
     *
     */
    boolean preserveTimeStamps = config.preserveTimeStamps

    /**
     * String representation of file permission.
     *
     */
    String defaultFilePermission = config.defaultFilePermission

    /**
     * Provides a builder style method to copy a file from the local
     * filesystem to a remotePath on a remote host.
     *
     * @param closure
     * @throws JSchException
     */
    public void execute(Closure closure) throws JSchException {
        run closure
        execute()
    }

    /**
     * Provides a method to copy a file from the local
     * filesystem to a remotePath on a remote host.
     *
     * @throws JSchException
     */
    public void execute() throws JSchException {
        try {
            log.trace("Request to copy file to remote host.")
            Session session = this.fetchSession()
            // exec 'scp -t remoteFile' remotely
            String command = "scp " + (preserveTimeStamps ? "-p" : "") + " -t " + remoteFile
            ChannelExec channel = session.openChannel("exec")
            channel.setCommand(command)

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream()
            InputStream inputStream = channel.getInputStream()

            channel.connect()

            if (checkAck(inputStream) != 0) {
                // There was an issue
                throw new JSchException("There was an error opening the initial input stream.")
            }

            File _lfile = new File(localFile)

            if (preserveTimeStamps) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0"
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n")
                out.write(command.bytes)
                out.flush()
                if (checkAck(inputStream) != 0) {
                    throw new JSchException("There was an error setting timestamp on file.")
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length()
            command = "C${defaultFilePermission ?: '0644'} " + filesize + " "

            if (localFile.lastIndexOf('/') > 0) {
                command += localFile.substring(localFile.lastIndexOf('/') + 1)
            }

            else {
                command += localFile
            }

            command += "\n"
            out.write(command.bytes)
            out.flush()

            if (checkAck(inputStream) != 0) {
                throw new JSchException("There was an error creating the file.")
            }

            // send a content of lfile
            fileInputStream = new FileInputStream(localFile)
            byte[] buf = new byte[1024]
            log.trace("Reading file info buffer.")
            while (true) {
                int len = fileInputStream.read(buf, 0, buf.length)
                if (len <= 0) {
                    break
                }
                out.write(buf, 0, len)
            }
            fileInputStream.close()
            fileInputStream = null
            // send '\0'
            buf[0] = 0
            out.write(buf, 0, 1)
            out.flush()
            if (checkAck(inputStream) != 0) {
                //System.exit(0)
                throw new JSchException("There was an error writing data into the file.")
            }
            out.close()
            channel.disconnect()
            session.disconnect()
        }
        catch (JSchException je) {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close()
                }
            }
            catch (Exception ee) {
            }

            throw je
        }
        catch (Exception e) {
            println(e)
            try {
                if (fileInputStream != null) {
                    fileInputStream.close()
                }
            }
            catch (Exception ee) {
            }
        }
    }
}