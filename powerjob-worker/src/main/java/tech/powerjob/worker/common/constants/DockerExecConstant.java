package tech.powerjob.worker.common.constants;

/**
 * @author: zmx
 * @date 2022/5/16
 */
public class DockerExecConstant {

    public static final String PYTHON = "docker run -v %s:/usr/src/python -w /usr/src/python gw_python:3.6.b python %s";
}
