package integration;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.webdriver.ChromeDriverFactory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static com.codeborne.selenide.WebDriverRunner.isChrome;
import static java.lang.System.nanoTime;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class ChromeProfileByFactoryTest extends IntegrationTest {
  private static final File downloadsFolder = new File(Configuration.downloadsFolder);
  private static final File chromedriverLog = new File(downloadsFolder, "chromedriver." + nanoTime());

  @BeforeEach
  void setUp() throws IOException {
    assumeThat(isChrome()).isTrue();

    closeWebDriver();
    Configuration.timeout = 1000;
    Configuration.browser = MyFactory.class.getName();
    FileUtils.write(chromedriverLog, "", UTF_8);
  }

  @Test
  void downloadsFilesToCustomFolder() throws IOException {
    openFile("page_with_uploads.html");
    $(byText("Download me")).shouldBe(visible);

    String log = readFileToString(chromedriverLog, UTF_8);
    assertThat(log).contains("\"excludeSwitches\": [ \"enable-automation\" ]");
    assertThat(log).contains("\"extensions\": [ \"Q3I");
    assertThat(log).contains("\"credentials_enable_service\": false");
    assertThat(log).contains("\"download.default_directory\": \"" + downloadsFolder.getAbsolutePath() + "\"");
    assertThat(log).contains("\"args\": [ \"--proxy-bypass-list=\\u003C-loopback>\", \"--no-sandbox\", \"--disable-3d-apis\" ]");
  }

  private static class MyFactory extends ChromeDriverFactory {
    @Override
    public WebDriver create(Config config, Browser browser, Proxy proxy) {
      System.setProperty("webdriver.chrome.logfile", chromedriverLog.getAbsolutePath());
      System.setProperty("webdriver.chrome.verboseLogging", "true");
      return super.create(config, browser, proxy);
    }

    @Override
    protected ChromeOptions createChromeOptions(Config config, Proxy proxy) {
      ChromeOptions options = super.createChromeOptions(config, proxy);
      options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
      options.addArguments(Arrays.asList("--no-sandbox", "--disable-3d-apis"));

      File extension = new File(currentThread().getContextClassLoader().getResource("get-crx.crx").getPath());
      options.addExtensions(extension);
      return options;
    }
  }
}
