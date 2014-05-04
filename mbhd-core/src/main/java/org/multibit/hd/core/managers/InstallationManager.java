package org.multibit.hd.core.managers;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.multibit.hd.core.events.ShutdownEvent;
import org.multibit.hd.core.files.SecureFiles;
import org.multibit.hd.core.utils.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 *  <p>Manager to provide the following to other core classes:</p>
 *  <ul>
 *  <li>Location of the installation directory</li>
 *  <li>Access the configuration file</li>
 * <li>Utility methods eg copying checkpoint files from installation directory</li>
 *  </ul>
 *  
 */
public class InstallationManager {

  private static final Logger log = LoggerFactory.getLogger(InstallationManager.class);

  public static final String MBHD_APP_NAME = "MultiBitHD";
  public static final String MBHD_PREFIX = "mbhd";
  public static final String MBHD_CONFIGURATION_FILE = MBHD_PREFIX + ".yaml";
  public static final String SPV_BLOCKCHAIN_SUFFIX = ".spvchain";

  public static final String CHECKPOINTS_SUFFIX = ".checkpoints";

  /**
   * The current application data directory
   */
  public static File currentApplicationDataDirectory = null;

  /**
   * A test flag to allow FEST tests to run efficiently
   */
  public static boolean unrestricted = false;

  /**
   * @param shutdownEvent The shutdown event
   */
  public static void onShutdownEvent(ShutdownEvent shutdownEvent) {

    currentApplicationDataDirectory = null;

  }

  /**
   * @return A reference to where the configuration file should be located
   */
  public static File getConfigurationFile() {

    return new File(getOrCreateApplicationDataDirectory().getAbsolutePath() + File.separator + MBHD_CONFIGURATION_FILE);

  }

  /**
   * <p>Get the directory for the user's application data, creating if not present</p>
   * <p>Checks a few OS-dependent locations first</p>
   */
  public static File getOrCreateApplicationDataDirectory() {

    if (currentApplicationDataDirectory != null) {
      return currentApplicationDataDirectory;
    }

    // Check the current working directory for the configuration file
    File multibitPropertiesFile = new File(MBHD_CONFIGURATION_FILE);
    if (multibitPropertiesFile.exists()) {
      return new File(".");
    }

    final String applicationDataDirectoryName;

    // Locations are OS-dependent
    if (OSUtils.isWindows()) {

      // Windows
      applicationDataDirectoryName = System.getenv("APPDATA") + File.separator + MBHD_APP_NAME;

    } else if (OSUtils.isMac()) {

      // OSX
      if ((new File("../../../../" + MBHD_CONFIGURATION_FILE)).exists()) {
        applicationDataDirectoryName = new File("../../../..").getAbsolutePath();
      } else {
        applicationDataDirectoryName = System.getProperty("user.home") + "/Library/Application Support/" + MBHD_APP_NAME;
      }
    } else {

      // Other (probably a Unix variant)
      applicationDataDirectoryName = System.getProperty("user.home") + "/" + MBHD_APP_NAME;
    }

    log.debug("Application data directory is '{}'", applicationDataDirectoryName);

    // Create the application data directory if it does not exist
    File applicationDataDirectory = new File(applicationDataDirectoryName);
    SecureFiles.verifyOrCreateDirectory(applicationDataDirectory);

    // Must be OK to be here so set this as the current
    currentApplicationDataDirectory = applicationDataDirectory;

    return applicationDataDirectory;
  }

  /**
   * Copy the checkpoints file from the MultiBitHD installation to the specified filename
   *
   * @param destinationCheckpointsFile The sink to receive the source checkpoints file
   */
  public static void copyCheckpointsTo(File destinationCheckpointsFile) throws IOException {

    Preconditions.checkNotNull(destinationCheckpointsFile, "'checkpointsFile' must be present");

    // TODO overwrite if larger/ newer
    if (!destinationCheckpointsFile.exists() || destinationCheckpointsFile.length() == 0) {

      log.debug("Copying checkpoints to '{}'", destinationCheckpointsFile);

      // Work out the source checkpoints (put into the program installation directory by the installer)
      File currentWorkingDirectory = new File(".");
      File sourceBlockCheckpointsFile = new File(currentWorkingDirectory.getAbsolutePath() + File.separator + MBHD_PREFIX + CHECKPOINTS_SUFFIX);

      // Prepare an input stream to the checkpoints
      final InputStream sourceCheckpointsStream;
      if (sourceBlockCheckpointsFile.exists()) {
        // Use the file system
        log.debug("Using source checkpoints from working directory.");
        sourceCheckpointsStream = new FileInputStream(sourceBlockCheckpointsFile);
      } else {
        // Use the classpath
        log.debug("Using source checkpoints from classpath.");
        sourceCheckpointsStream = InstallationManager.class.getResourceAsStream("/mbhd.checkpoints");
      }

      // Create the output stream
      FileOutputStream sinkCheckpointsStream = new FileOutputStream(destinationCheckpointsFile);

      // Copy the checkpoints
      long bytes = ByteStreams.copy(sourceCheckpointsStream, sinkCheckpointsStream);

      // Clean up
      sourceCheckpointsStream.close();
      sinkCheckpointsStream.flush();
      sinkCheckpointsStream.close();

      log.debug("New checkpoints are {} bytes in length.", bytes);

      if (bytes < 13_000) {
        log.warn("Checkpoints are short.");
      }

    } else {

      log.debug("Checkpoints already exist.");

    }
  }

}
