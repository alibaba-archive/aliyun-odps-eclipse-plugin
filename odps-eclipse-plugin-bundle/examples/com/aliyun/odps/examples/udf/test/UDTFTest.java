package com.aliyun.odps.examples.udf.test;

import java.io.IOException;
import java.util.List;

import com.aliyun.odps.udf.UDFException;
import com.aliyun.odps.udf.local.LocalRunException;
import com.aliyun.odps.udf.local.datasource.InputSource;
import com.aliyun.odps.udf.local.datasource.TableInputSource;
import com.aliyun.odps.udf.local.runner.BaseRunner;
import com.aliyun.odps.udf.local.runner.UDTFRunner;

/**
 * 
 * you can also write an UT
 * 
 */
public class UDTFTest {
  public static void main(String[] args) throws LocalRunException, UDFException, IOException {
    // //////////////test1: simple input/////////////////
    BaseRunner runner = new UDTFRunner(null, "com.aliyun.odps.examples.udf.UDTFExample");
    runner.feed(new Object[] { "one", "one" }).feed(new Object[] { "three", "three" })
        .feed(new Object[] { "four", "four" });
    List<Object[]> out = runner.yield();
    TestUtil.assertEquals(3 + "", out.size() + "");
    TestUtil.assertEquals("one,3", TestUtil.join(out.get(0)));
    TestUtil.assertEquals("three,5", TestUtil.join(out.get(1)));
    TestUtil.assertEquals("four,4", TestUtil.join(out.get(2)));

    // //////////////test2: input from table/////////////////
    runner = new UDTFRunner(TestUtil.getOdps(), "com.aliyun.odps.examples.udf.UDTFExample");
    String project = "example_project";
    String table = "wc_in2";
    String[] partitions = new String[] { "p2=1", "p1=2" };
    String[] columns = new String[] { "colc", "cola" };

    InputSource inputSource = new TableInputSource(project, table, partitions, columns);
    Object[] data;
    while ((data = inputSource.getNextRow()) != null) {
      runner.feed(data);
    }
    out = runner.yield();
    TestUtil.assertEquals(3 + "", out.size() + "");
    TestUtil.assertEquals("three3,6", TestUtil.join(out.get(0)));
    TestUtil.assertEquals("three3,6", TestUtil.join(out.get(1)));
    TestUtil.assertEquals("three3,6", TestUtil.join(out.get(2)));

    // //////////////test3: resource test/////////////////
    runner = new UDTFRunner(TestUtil.getOdps(), "com.aliyun.odps.examples.udf.UDTFResource");
    runner.feed(new Object[] { "one", "one" }).feed(new Object[] { "three", "three" })
        .feed(new Object[] { "four", "four" });
    out = runner.yield();
    TestUtil.assertEquals(3 + "", out.size() + "");
    TestUtil.assertEquals(
        "one,3,fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
        TestUtil.join(out.get(0)));
    TestUtil.assertEquals(
        "three,5,fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
        TestUtil.join(out.get(1)));
    TestUtil.assertEquals(
        "four,4,fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
        TestUtil.join(out.get(2)));

    System.out.println("Pass");
  }
}
