package org.skellig.teststep.processor.web3

import org.skellig.teststep.processor.web3.model.Web3TestStep
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthLog
import java.util.concurrent.TimeUnit

class EventTaskExecutor {

    private val typeReferencesMap = TypeReferencesMap()

    fun execute(testStep: Web3TestStep, web3Node: Web3j): Any? {
        val typeReferences = testStep.returns?.map {
            typeReferencesMap.get(it) ?: error("No type reference found for type '$it'. Supported types are: $typeReferencesMap")
        }

        return extractEvents(Event(testStep.event, typeReferences), testStep, web3Node)
    }

    private fun extractEvents(event: Event, testStep: Web3TestStep, web3Node: Web3j): Any? {
        val ethFilter: EthFilter = testStep.filter?.let {
            val address = it["address"] as String?
            val addresses = it["addresses"] as List<String>?
            val fromBlock = it["fromBlock"] as String?
            val toBlock = it["toBlock"] as String?
            val blockHash = it["toBlock"] as String?

            if (address != null && fromBlock != null && toBlock != null)
                EthFilter(DefaultBlockParameterName.fromString(fromBlock), DefaultBlockParameterName.fromString(toBlock), address)
            else if (addresses != null && fromBlock != null && toBlock != null)
                EthFilter(DefaultBlockParameterName.fromString(fromBlock), DefaultBlockParameterName.fromString(toBlock), addresses)
            else if (blockHash != null && address == null) EthFilter(blockHash)
            else if (blockHash != null && address != null) EthFilter(blockHash, address)
            else EthFilter()
        } ?: EthFilter()
        ethFilter.addSingleTopic(EventEncoder.encode(event))

        val ethLog = web3Node.ethGetLogs(ethFilter).sendAsync().get(testStep.timeout.toLong(), TimeUnit.MILLISECONDS)
        return if (ethLog.hasError()) {
            ethLog.error
        } else {
            val raw = testStep.filter?.get("raw")?.toString()
            ethLog.logs.map {
                if ("true" == raw) it
                else if (it is EthLog.LogObject) FunctionReturnDecoder.decode(it.data, event.nonIndexedParameters);
                else it
            }
        }
    }
}