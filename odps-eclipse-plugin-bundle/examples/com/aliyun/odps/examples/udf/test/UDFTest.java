package com.aliyun.odps.examples.udf.test;

import java.io.IOException;
import java.util.List;

import com.aliyun.odps.udf.local.LocalRunException;
import com.aliyun.odps.udf.local.datasource.InputSource;
import com.aliyun.odps.udf.local.datasource.TableInputSource;
import com.aliyun.odps.udf.local.runner.BaseRunner;
import com.aliyun.odps.udf.local.runner.UDFRunner;

/**
 * 
 * you can also write an UT
 * 
 */
public class UDFTest {

  public static void main(String[] args) throws LocalRunException, IOException {

    // //////////////test1: simple input/////////////////
    BaseRunner runner = new UDFRunner(null, "com.aliyun.odps.examples.udf.UDFExample");
    runner.feed(new Object[] { "one", "one" }).feed(new Object[] { "three", "three" })
        .feed(new Object[] { "four", "four" });
    List<Object[]> out = runner.yield();

    TestUtil.assertEquals("3", out.size() + "");
    TestUtil.assertEquals("ss2s:one,one", TestUtil.join(out.get(0)));
    TestUtil.assertEquals("ss2s:three,three", TestUtil.join(out.get(1)));
    TestUtil.assertEquals("ss2s:four,four", TestUtil.join(out.get(2)));

    // //////////////test2: input from table/////////////////
    runner = new UDFRunner(TestUtil.getOdps(), "com.aliyun.odps.examples.udf.UDFExample");
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
    TestUtil.assertEquals("3", out.size() + "");
    TestUtil.assertEquals("ss2s:three3,three1", TestUtil.join(out.get(0)));
    TestUtil.assertEquals("ss2s:three3,three1", TestUtil.join(out.get(1)));
    TestUtil.assertEquals("ss2s:three3,three1", TestUtil.join(out.get(2)));

    // //////////////test3: resource test/////////////////
    runner = new UDFRunner(TestUtil.getOdps(), "com.aliyun.odps.examples.udf.UDFResource");
    runner.feed(new Object[] { "one", "one" }).feed(new Object[] { "three", "three" })
        .feed(new Object[] { "four", "four" });
    out = runner.yield();

    TestUtil.assertEquals(3 + "", out.size() + "");
    TestUtil
        .assertEquals(
            "ss2s:one,one|fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
            TestUtil.join(out.get(0)));
    TestUtil
        .assertEquals(
            "ss2s:three,three|fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
            TestUtil.join(out.get(1)));
    TestUtil
        .assertEquals(
            "ss2s:four,four|fileResourceLineCount=3|tableResource1RecordCount=4|tableResource2RecordCount=4",
            TestUtil.join(out.get(2)));

    System.out.println("Pass");

  }

}
