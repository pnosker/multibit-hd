package org.multibit.hd.ui.fest.requirements;

import com.google.common.collect.Maps;
import org.fest.swing.fixture.FrameFixture;
import org.multibit.hd.ui.fest.use_cases.sidebar.help.ShowHelpScreenUseCase;

import java.util.Map;

/**
 * <p>FEST Swing UI test to provide:</p>
 * <ul>
 * <li>Exercise the "help" screen to verify the contents show correctly</li>
 * </ul>
 *
 * @since 0.0.1
 *
 */
public class HelpScreenRequirements {

  public static void verifyUsing(FrameFixture window) {

    Map<String,Object> parameters = Maps.newHashMap();

    // Select the help screen
    new ShowHelpScreenUseCase(window).execute(parameters);

  }
}
