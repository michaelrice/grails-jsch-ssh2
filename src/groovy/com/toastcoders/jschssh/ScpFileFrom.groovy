package com.toastcoders.jschssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.apache.log4j.Logger

/**
 * User: Michael Rice
 * Project: grails-jsch-ssh2
 * Website: http://www.errr-online.com/
 * Github: https://github.com/michaelrice
 * Date: 5/18/14
 * Time: 10:56 PM
 * Licenses: MIT http://opensource.org/licenses/MIT
 */

class ScpFileFrom extends ConnectionInfo {

    /**
     * Logger instance.
     */
    private Logger log = Logger.getLogger(ScpFileFrom)

    InputStream fileInputStream = null
    /**
     * FileInputStream of the local file that will be getting sent to the
     * remote server.
     *
     */
    OutputStream outputStream = null

    /**
     * Full path to the local file. The tomcat user must have write
     * access to this file.
     *
     */
    String localFile

    /**
     * Full path to remote file.
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
        log.trace("Fetching file from remote server.")
        FileOutputStream newFileOutputStream = null
        try {

            String prefix = null
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator
                log.trace("Setting prefix to: ${prefix}")
            }

            log.trace("Fetching new session.")
            Session session = this.fetchSession()

            // exec 'scp -f remoteFile' remotely
            String command = "scp -f " + remoteFile
            log.trace("Setting command: ${command}")
            ChannelExec channel = session.openChannel("exec")
            channel.setCommand(command)

            // Get I/O streams for remote scp.
            outputStream = channel.getOutputStream()
            fileInputStream = channel.getInputStream()

            channel.connect()

            byte[] buffer = new byte[1024]

            // send '\0'
            buffer[0] = 0
            outputStream.write(buffer, 0, 1)
            outputStream.flush()

            String t
            while (true) {
                int c = checkAck(fileInputStream)
                log.trace("c == ${c}")
                // Ascii C is 67
                if (c != 'C') {
                    log.trace("c != C ${c}")
                    break
                }

                // read '0644 '
                fileInputStream.read(buffer, 0, 5)

                long filesize = 0L
                while (true) {
                    if (fileInputStream.read(buffer, 0, 1) < 0 || buffer[0] == ' ') {
                        log.trace("An error has happened trying to read the inputStream")
                        break
                    }

                    log.trace("Buffer[0] == ${buffer[0]}")
                    t = buffer[0] as String
                    log.trace("buffer[0] ${buffer[0].getClass()}")
                    log.trace("t: ${t} class: ${t.getClass()} filesize: ${filesize}")
                    filesize = filesize * 10L + (long) (t.minus('0') as int)
                    log.trace("filesize again: ${filesize}")
                }

                log.trace("getting ready to read input in for loop. filesize: ${filesize}")
                String file = null
                for (int i = 0; ; i++) {
                    fileInputStream.read(buffer, i, 1)
                    log.trace("chomp chomp ${i} buffer[i] == ${buffer[i]}")
                    if (buffer[i] == (byte) 0x0a) {
                        file = new String(buffer, 0, i)
                        log.trace("found new line breaking!!")
                        break
                    }
                }

                // send '\0'
                buffer[0] = 0
                outputStream.write(buffer, 0, 1)
                outputStream.flush()
                // read a content of localFile
                newFileOutputStream = new FileOutputStream(prefix == null ? localFile : prefix + file)
                int foo
                while (true) {
                    log.trace("Another trip through this while true loop")
                    if (buffer.length < filesize) {
                        // TODO remove this log
                        log.trace("Buffer length is < filesize. Setting foo =  ${buffer.length} < ${filesize}")
                        foo = buffer.length
                    }
                    else {
                        // TODO remove this log
                        log.trace("filesize is > buffer length. Setting foo = ${filesize} > ${buffer.length} before foo: ${foo}")
                        foo = (int) filesize
                    }
                    // TODO remove this log
                    log.trace("Reading from fileInputStream at ${foo}")
                    foo = fileInputStream.read(buffer, 0, foo)
                    log.trace("foo: ${foo}")
                    if (foo < 0) {
                        // error
                        log.trace("An error has been detected. foo < 0 ${foo}")
                        break
                    }
                    log.trace("Writing buffer we just read into the new file.")
                    newFileOutputStream.write(buffer, 0, foo)
                    filesize -= foo
                    log.trace("filesize: ${filesize} foo: ${foo}")
                    if (filesize == 0L) {
                        log.trace("Completed writing the newFile to disk")
                        break
                    }
                    log.trace("filesize: ${filesize}")
                }
                log.trace("Closing newFileOutputStream since file is done.")
                newFileOutputStream.close()
                log.trace("Setting newFileOutputStream to null")
                newFileOutputStream = null

                if (checkAck(fileInputStream) != 0) {
                    throw new JSchException("There was an error writing data into the file.")
                }

                // send '\0'
                buffer[0] = 0;
                outputStream.write(buffer, 0, 1);
                outputStream.flush()
            }
            session.disconnect()
            channel.disconnect()
        }
        catch (Exception e) {

            log.trace("Exception caught.", e);
            try {
                if (outputStream != null) {
                    outputStream.close()
                }
            }
            catch (Exception ee) {
                log.trace("Another exception caught.", ee)
            }
        }
    }
}