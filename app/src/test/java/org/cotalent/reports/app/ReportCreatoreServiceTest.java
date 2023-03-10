package org.cotalent.reports.app;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportCreatoreServiceTest {

  private ReportCreatorService sut = new ReportCreatorService(new TradeConsumer());

  @Rule
  public TemporaryFolder baseFolder = new TemporaryFolder();
  private String emptyFolderName;

  @Before
  public void createTmpFolders() throws Exception {
    emptyFolderName = baseFolder.newFolder("empty").getPath();
    try (OutputStream target = new FileOutputStream(new File(baseFolder.newFolder("20220116"), "Input.txt"))) {
      FileCopyUtils.copy(getClass().getResourceAsStream("/Input.txt"), target);
    }
  }

  @After
  public void clean() throws Exception {
    baseFolder.delete();
  }

  private void updateSutField(String fieldName, Object value) throws Exception {
    Field field = ReportCreatorService.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    ReflectionUtils.setField(field, sut, value);
  }

  @Test
  public void shouldDoNothingforEmptyInput() throws Exception {
    updateSutField("inputBaseFolder", emptyFolderName);
    updateSutField("outputBaseFolder", emptyFolderName);
    sut.scan();

    File[] createdFiles = new File(emptyFolderName).listFiles();
    assertThat(createdFiles, emptyArray());
  }

  @Test
  public void shouldReadInput() throws Exception {
    String baseFolderName = baseFolder.getRoot().getPath();
    updateSutField("inputBaseFolder", baseFolderName);
    updateSutField("outputBaseFolder", baseFolderName);
    sut.scan();
    String expectedFileContent = "Client_Information,Product_Information,Total_Transaction_Amount\n" +
        "CL_4321_3_1,CME_FU_N1_2010-09-10,-79\n" +
        "CL_1234_3_1,CME_FU_N1_2010-09-10,285\n" +
        "CL_4321_2_1,SGX_FU_NK_2010-09-10,46\n" +
        "CL_1234_2_1,SGX_FU_NK_2010-09-10,-52\n" +
        "CL_1234_3_1,CME_FU_NK._2010-09-10,-215\n";
    String actualFileContent = Files.readString(
        Paths.get(baseFolderName + File.separator + "20220116" + File.separator + "Output.csv"),
        Charset.defaultCharset());
    log.info("Actual output Content: \n[{}]", actualFileContent);
    assertThat(actualFileContent, is(expectedFileContent));
  }

}
