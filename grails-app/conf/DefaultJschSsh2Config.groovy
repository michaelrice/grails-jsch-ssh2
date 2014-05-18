/**
 * Created with IntelliJ IDEA.
 * User: Michael Rice
 * Twitter: @errr_
 * Website: http://www.errr-online.com/
 * Github: https://github.com/michaelrice
 * Date: 5/11/2014
 * Time: 4:05 PM
 * Licenses: MIT http://opensource.org/licenses/MIT
 */

jschSsh2 {
    username = null
    password = null
    keyFile = null
    keyFilePassword = null
    port = 22
    strictHostKeyChecking = "yes"
    knownHostsFile = "~/.ssh/known_hosts"
    sshConfigFile = "~/.ssh/config"
    connectionTimeout = 0
    preserveTimeStamps = false
    // Normal File Read + Write for user,
    // Read for group and Everyone
    defaultFilePermission = "0644"
}