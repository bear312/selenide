package com.codeborne.selenide.webdriver;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class MergeableCapabilitiesTest {
  @Test
  void mergesChromeOptions() {
    ChromeOptions base = new ChromeOptions();
    base.addArguments("--a", "--b");
    ChromeOptions extra = new ChromeOptions();
    extra.addArguments("--c", "--d");
    extra.setBinary("/usr/local/chrome.exe");

    MergeableCapabilities result = new MergeableCapabilities(base, extra);

    assertThat(result.asMap()).containsEntry("goog:chromeOptions", ImmutableMap.of(
      "args", asList("--a", "--b", "--c", "--d"),
      "binary", "/usr/local/chrome.exe",
      "extensions", emptyList()
    ));
  }
}
