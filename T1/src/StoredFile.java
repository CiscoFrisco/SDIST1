import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

class StoredFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8932985840046828L;
	private String fileName;
	private String fileId;

	StoredFile(String fileName) {
		this.fileName = fileName;

		this.fileId = encryptFileId(fileName);
	}

	public static String encryptFileId(String fileName) {

		String dateModified = "", owner = "";

		try {
			Path file = Paths.get(fileName);
			BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

			dateModified = attr.lastModifiedTime().toString();
			owner = Files.getOwner(file).getName();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Utils.getSHA(fileName + "-" + dateModified + "-" + owner);
	}

	public ArrayList<Chunk> splitFile() throws IOException {

		int chunkSize = 64 * 1000;// 64KByte
		byte[] buffer = new byte[chunkSize];
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		File f = new File(this.fileName);
		int chunkNo = 0, bytesAmount = 0;

		// try-with-resources to ensure closing stream
		try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {


			for (; (bytesAmount = bis.read(buffer)) > 0; chunkNo++)
				chunks.add(new Chunk(fileId, chunkNo, buffer, bytesAmount));
		}

		if(f.length() % chunkSize == 0)
			chunks.add(new Chunk(fileId, chunkNo, buffer, 0));

		return chunks;
	}

	public String getFileId() {
		return fileId;
	}
}