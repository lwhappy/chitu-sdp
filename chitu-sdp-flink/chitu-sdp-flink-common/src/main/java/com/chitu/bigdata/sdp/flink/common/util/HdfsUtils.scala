
package com.chitu.bigdata.sdp.flink.common.util

import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.fs._
import org.apache.hadoop.hdfs.DistributedFileSystem
import org.apache.hadoop.io.IOUtils
import org.apache.hadoop.ipc.RPC

import java.io.{ByteArrayOutputStream, FileWriter, IOException}
import java.net.InetSocketAddress
import java.util
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object HdfsUtils extends Logger {

  def getDefaultFS: String = HadoopUtils.hadoopConf.get(FileSystem.FS_DEFAULT_NAME_KEY)

  def list(src: String): List[FileStatus] = HadoopUtils.hdfs.listStatus(getPath(src)).toList

  def getCheckPointPath(fileNamePrefix: String): String = {
    val filterList = list(fileNamePrefix).map(item => item.getPath.getName).filter(item => item.contains("chk-"))
    if(filterList!=null && filterList.nonEmpty) {
      filterList.maxBy(_.toString)
    } else {
      ""
    }

  }

  def getCheckPointPaths(fileNamePrefix: String): util.List[FileStatus] = {
    val bool = exists(fileNamePrefix)
    if(!bool) {
      return  new util.ArrayList[FileStatus]();
    }
    val fss = list(fileNamePrefix).filter(item => item.getPath.getName.contains("chk-"))
    if(fss !=null && fss.nonEmpty){
       fss.sortBy(item => item.getModificationTime).reverse.toList.asJava
    }else{
      return  new util.ArrayList[FileStatus]();
    }
  }

  def movie(src: String, dst: String): Unit = HadoopUtils.hdfs.rename(getPath(src), getPath(dst))

  def mkdirs(path: String): Unit = HadoopUtils.hdfs.mkdirs(getPath(path))

  def copyHdfs(src: String, dst: String, delSrc: Boolean = false, overwrite: Boolean = true): Unit =
    FileUtil.copy(HadoopUtils.hdfs, getPath(src), HadoopUtils.hdfs, getPath(dst), delSrc, overwrite, HadoopUtils.hadoopConf)

  def copyHdfsDir(src: String, dst: String, delSrc: Boolean = false, overwrite: Boolean = true): Unit = {
    list(src).foreach(x => FileUtil.copy(HadoopUtils.hdfs, x, HadoopUtils.hdfs, getPath(dst), delSrc, overwrite, HadoopUtils.hadoopConf))
  }

  def upload(src: String, dst: String, delSrc: Boolean = false, overwrite: Boolean = true): Unit =
    HadoopUtils.hdfs.copyFromLocalFile(delSrc, overwrite, getPath(src), getPath(dst))

  def upload2(srcs: Array[String], dst: String, delSrc: Boolean = false, overwrite: Boolean = true): Unit =
    HadoopUtils.hdfs.copyFromLocalFile(delSrc, overwrite, srcs.map(getPath), getPath(dst))

  def download(src: String, dst: String, delSrc: Boolean = false, useRawLocalFileSystem: Boolean = false): Unit =
    HadoopUtils.hdfs.copyToLocalFile(delSrc, getPath(src), getPath(dst), useRawLocalFileSystem)

  def getNameNode: String = Try(getAddressOfActive(HadoopUtils.hdfs).getHostString) match {
    case Success(value) => value
    case Failure(exception) => throw exception
  }

  /**
   * 在hdfs 上创建一个新的文件，将某些数据写入到hdfs中
   *
   * @param fileName
   * @param content
   * @throws
   */
  def create(fileName: String, content: String): Unit = {
    val path: Path = getPath(fileName)
    require(HadoopUtils.hdfs.exists(path), s"[SDP] hdfs $fileName is exists!! ")
    val outputStream: FSDataOutputStream = HadoopUtils.hdfs.create(path)
    outputStream.writeUTF(content)
    outputStream.flush()
    outputStream.close()
  }

  def exists(path: String): Boolean = HadoopUtils.hdfs.exists(getPath(path))

  def read(fileName: String): String = {
    val path: Path = getPath(fileName)
    require(HadoopUtils.hdfs.exists(path) && !HadoopUtils.hdfs.isDirectory(path), s"[SDP] path:$fileName not exists or isDirectory ")
    val in = HadoopUtils.hdfs.open(path)
    val out = new ByteArrayOutputStream()
    IOUtils.copyBytes(in, out, 4096, false)
    out.flush()
    IOUtils.closeStream(in)
    IOUtils.closeStream(out)
    new String(out.toByteArray)
  }

  def delete(src: String): Unit = {
    val path: Path = getPath(src)
    if (HadoopUtils.hdfs.exists(path)) {
      HadoopUtils.hdfs.delete(path, true)
    } else {
      logWarn(s"hdfs delete $src,but file $src is not exists!")
    }
  }

  def fileMd5(fileName: String): String = {
    val path = getPath(fileName)
    val in = HadoopUtils.hdfs.open(path)
    Try(DigestUtils.md5Hex(in)) match {
      case Success(s) =>
        in.close()
        s
      case Failure(e) =>
        in.close()
        throw e
    }
  }

  def downToLocal(hdfsPath: String, localPath: String): Unit = {
    val path: Path = getPath(hdfsPath)
    val input: FSDataInputStream = HadoopUtils.hdfs.open(path)
    val content: String = input.readUTF
    val fw: FileWriter = new FileWriter(localPath)
    fw.write(content)
    fw.close()
    input.close()
  }

  private[this] def getPath(hdfsPath: String) = new Path(hdfsPath)

  @throws[IOException]
  def getAddressOfActive(fs: FileSystem): InetSocketAddress = {
    if (!fs.isInstanceOf[DistributedFileSystem]) {
      throw new IllegalArgumentException(s"FileSystem $fs is not a DFS.")
    }
    // force client address resolution.
    fs.exists(new Path("/"))
    val dfs = fs.asInstanceOf[DistributedFileSystem]
    val dfsClient = dfs.getClient
    RPC.getServerAddress(dfsClient.getNamenode)
  }

}
