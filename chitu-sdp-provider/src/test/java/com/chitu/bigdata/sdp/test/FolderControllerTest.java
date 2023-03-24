package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.bo.SdpFolderBO;
import com.chitu.cloud.web.test.BaseTest;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;

public class FolderControllerTest extends BaseTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Test
    public void testAddFolder() throws Exception {
        Long projectId = 139L;
        String folderName = RandomStringUtils.randomAlphanumeric(8);
        SdpFolderBO folder = new SdpFolderBO();
        folder.setFolderName(folderName);
        folder.setProjectId(projectId);

        given().
                webAppContextSetup(webApplicationContext).
                headers("X-uid", "999999").
                body(folder).
                contentType(ContentType.JSON).
                when().
                post("/folder/addFolder").
                then().
                statusCode(200).
                body("data.folderName", equalTo(folderName));
    }

}
