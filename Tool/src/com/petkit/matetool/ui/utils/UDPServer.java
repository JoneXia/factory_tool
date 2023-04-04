package com.petkit.matetool.ui.utils;

import com.petkit.matetool.model.UDPDevice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    public interface UDPResultListener {
        void onResult(UDPDevice device);
    }

    private boolean isAbort = false;
    private UDPResultListener mListener;
    private DatagramSocket serverSocket;


    public void startScan() throws Exception {

        // 创建一个UDP socket并绑定到指定的端口
        if (serverSocket == null) {
            serverSocket = new DatagramSocket(8002);
            serverSocket.setReuseAddress(true);
        }

        byte[] receiveData = new byte[1024];

        new Thread(){
            @Override
            public void run() {
                super.run();
                while (!isAbort) {

                    // 创建一个数据报文包对象
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // 从UDP socket接收数据包
                    try {
                        serverSocket.receive(receivePacket);


                        // 从接收数据包中获取数据
                        String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // 获取数据包来源的IP地址和端口号
                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();

                        // 输出接收到的数据和来源信息
                        System.out.println("Received: " + sentence);
                        System.out.println("From: " + IPAddress + ":" + port);

                        if (mListener != null) {
                            String ip = String.valueOf(IPAddress);
                            ip = ip.replace("/", "");
                            mListener.onResult(new UDPDevice(sentence, ip, port));
                        }

                        // 回复一个UDP数据包到来源IP地址和端口号
//                        String capitalizedSentence = sentence.toUpperCase();
//                        byte[] sendData = capitalizedSentence.getBytes();
//                        DatagramPacket sendPacket =
//                                new DatagramPacket(sendData, sendData.length, IPAddress, port);
//                        serverSocket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    public void setAbort(boolean abort) {
        isAbort = abort;
        if (isAbort) {
            serverSocket.close();
            serverSocket = null;
        }
    }

    public boolean isAbort() {
        return isAbort;
    }

    public void setListener(UDPResultListener listener) {
        mListener = listener;
    }
}
