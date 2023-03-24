package com.chitu.bigdata.sdp.flink.submit.service

import org.apache.flink.configuration.Configuration

case class SubmitResponse(clusterId: String, configuration: Configuration, jobManagerAddress: String) {

}
