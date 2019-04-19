package com.geeetech.administrator.easyprint;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    private void getTest() throws Exception{
        System.out.print(getJsonData());
    }
    public JSONObject getJsonData() {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        try {
            obj.put("name","dad");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            js.put("action","update");
            js.put("target","user_pprofile");
            js.put("object",obj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }
}