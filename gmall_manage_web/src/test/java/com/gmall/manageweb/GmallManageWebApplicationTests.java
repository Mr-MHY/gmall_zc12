package com.gmall.manageweb;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

	@Test
	public void contextLoads() {
	}
	@Test//http://file.gmall.com/group1/M00/00/00/wKjcgl11FqmAeRfrAAD5svk0iKM712.jpg
	public void uploadFile() throws IOException, MyException {
		// 1
		String file = this.getClass().getResource("/tracker.conf").getFile();
		ClientGlobal.init(file);
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageClient storageClient = new StorageClient(trackerServer,null);

		String[] upload_file = storageClient.upload_file("F:\\1_assemble\\My Pictures\\pass\\IDOL\\webp.jpg", "jpg", null);
		for (int i = 0; i < upload_file.length; i++) {// TODO F:\1_assemble\My Pictures\pass\IDOL\webp.jpg
			String s = upload_file[i];
			System.out.println("s="+s);
		}

	}
}
