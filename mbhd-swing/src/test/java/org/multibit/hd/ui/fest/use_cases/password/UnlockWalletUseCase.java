package org.multibit.hd.ui.fest.use_cases.password;

import org.fest.swing.fixture.FrameFixture;
import org.multibit.hd.ui.fest.use_cases.AbstractFestUseCase;
import org.multibit.hd.ui.languages.MessageKey;

import java.util.Map;

/**
 * <p>Use case to provide the following to FEST testing:</p>
 * <ul>
 * <li>Unlock a wallet</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class UnlockWalletUseCase extends AbstractFestUseCase {

  public UnlockWalletUseCase(FrameFixture window) {
    super(window);
  }

  @Override
  public void execute(Map<String, Object> parameters) {

    window.textBox("password").enterText("abc123");
    window.button(MessageKey.PASSWORD_UNLOCK.getKey()).click();

  }

}
