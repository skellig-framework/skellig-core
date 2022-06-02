package org.skellig.teststep.processor.web3

import org.skellig.teststep.processor.web3.model.Web3TestStep
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class Web3MethodExecutor {

    private val typeReferencesMap = TypeReferencesMap()

    private val functions = mapOf(
        Pair("web3_sha3") { web3: Web3j, params: List<Any> -> runAsync(web3.web3Sha3(params[0] as String)) },
        Pair("net_peerCount") { web3: Web3j, _: List<Any> -> runAsync(web3.netPeerCount()) },
        Pair("admin_nodeInfo") { web3: Web3j, _: List<Any> -> runAsync(web3.adminNodeInfo()) },
        Pair("admin_peers") { web3: Web3j, _: List<Any> -> runAsync(web3.adminPeers()) },
        Pair("admin_datadir") { web3: Web3j, _: List<Any> -> runAsync(web3.adminDataDir()) },
        Pair("eth_chainId") { web3: Web3j, _: List<Any> -> runAsync(web3.ethChainId()) },
        Pair("eth_coinbase") { web3: Web3j, _: List<Any> -> runAsync(web3.ethCoinbase()) },
        Pair("eth_syncing") { web3: Web3j, _: List<Any> -> runAsync(web3.ethSyncing()) },
        Pair("eth_hashrate") { web3: Web3j, _: List<Any> -> runAsync(web3.ethHashrate()) },
        Pair("eth_gasPrice") { web3: Web3j, _: List<Any> -> runAsync(web3.ethGasPrice()) },
        Pair("eth_maxPriorityFeePerGas") { web3: Web3j, _: List<Any> -> runAsync(web3.ethMaxPriorityFeePerGas()) },
        Pair("eth_feeHistory") { web3: Web3j, params: List<Any> ->
            runAsync(
                web3.ethFeeHistory(
                    params[0].toString().toInt(),
                    getBlockParameter(params[1]),
                    (params[2] as List<*>).mapNotNull { it.toString().toDouble() })
            )
        },
        Pair("eth_accounts") { web3: Web3j, _: List<Any> -> runAsync(web3.ethAccounts()) },
        Pair("eth_blockNumber") { web3: Web3j, _: List<Any> -> runAsync(web3.ethBlockNumber()) },
        Pair("eth_getBalance") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetBalance(params[0] as String, getBlockParameter(params.getOrNull(1))))
        },
        Pair("eth_getStorageAt") { web3: Web3j, params: List<Any> ->
            runAsync(
                web3.ethGetStorageAt(
                    params[0] as String,
                    params[1].toString().toBigInteger(),
                    getBlockParameter(params.getOrNull(2))
                )
            )
        },
        Pair("eth_getTransactionCount") { web3: Web3j, params: List<Any> ->
            runAsync(
                web3.ethGetTransactionCount(
                    params[0] as String,
                    getBlockParameter(params.getOrNull(1))
                )
            )
        },
        Pair("eth_getBlockTransactionCountByHash") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetBlockTransactionCountByHash(params[0] as String))
        },
        Pair("eth_getBlockTransactionCountByNumber") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetBlockTransactionCountByNumber(getBlockParameter(params[0])))
        },
        Pair("eth_getUncleCountByBlockHash") { web3: Web3j, params: List<Any> -> runAsync(web3.ethGetUncleCountByBlockHash(params[0] as String)) },
        Pair("eth_getUncleCountByBlockNumber") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetUncleCountByBlockNumber(getBlockParameter(params.getOrNull(0))))
        },
        Pair("eth_getCode") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetCode(params[0] as String, getBlockParameter(params.getOrNull(0))))
        },
        Pair("eth_sign") { web3: Web3j, params: List<Any> -> runAsync(web3.ethSign(params[0] as String, params[1] as String)) },
        Pair("eth_sendTransaction") { web3: Web3j, params: List<Any> ->
            val rawTransaction = params[0] as Map<String, Any>
            val from = rawTransaction["from"] as String
            val to = rawTransaction["to"] as String
            val gas = toBigInteger(rawTransaction["gas"])
            val gasPrice = toBigInteger(rawTransaction["gasPrice"])
            val value = toBigInteger(rawTransaction["value"])
            val data = rawTransaction["data"]

            val encodedFunction = if (data is Map<*, *>) FunctionEncoder.encode(createFunction(data as Map<String, Any>)) else data as String
            val nonce = getNonce(from, web3)

            runAsync(web3.ethSendTransaction(Transaction.createFunctionCallTransaction(from, nonce, gasPrice, gas, to, value, encodedFunction)))
        },
        Pair("eth_sendRawTransaction") { web3: Web3j, params: List<Any> ->
            val rawTransaction = params[0] as Map<String, Any>
            val from = rawTransaction["from"] as String
            val to = rawTransaction["to"] as String
            val gas = toBigInteger(rawTransaction["gas"])
            val gasPrice = toBigInteger(rawTransaction["gasPrice"])
            val value = toBigInteger(rawTransaction["value"])
            val data = rawTransaction["data"]
            val encodedFunction = if (data is Map<*, *>) FunctionEncoder.encode(createFunction(data as Map<String, Any>)) else data as String
            val nonce = getNonce(from, web3)

            val signedMessage = TransactionEncoder.signMessage(
                RawTransaction.createTransaction(nonce, gasPrice, gas, to, value, encodedFunction), Credentials.create(from)
            )

            runAsync(web3.ethSendRawTransaction(Numeric.toHexString(signedMessage)))
        },
        Pair("eth_call") { web3: Web3j, params: List<Any> ->
            val rawTransaction = params[0] as Map<String, Any>
            val from = rawTransaction["from"] as String
            val to = rawTransaction["to"] as String
            val block = rawTransaction["block"] as String?
            val weiValue = toBigInteger(rawTransaction["weiValue"])
            val data = rawTransaction["data"]
            var function: Function? = null
            val encodedFunction = if (data is Map<*, *>) {
                function = createFunction(data as Map<String, Any>)
                FunctionEncoder.encode(function)
            } else data as String

            val response = runAsync(
                web3.ethCall(
                    Transaction.createEthCallTransaction(from, to, encodedFunction, weiValue),
                    getBlockParameter(block ?: params.getOrNull(1))
                )
            ) as EthCall

            function?.let {
                if (!response.hasError() && function.outputParameters.isNotEmpty())
                    FunctionReturnDecoder.decode(response.value, function.outputParameters)
                else response
            } ?: response
        },
        Pair("eth_estimateGas") { web3: Web3j, params: List<Any> ->
            val rawTransaction = params[0] as Map<String, Any>
            val from = rawTransaction["from"] as String
            val gas = toBigInteger(rawTransaction["gas"])
            val gasPrice = toBigInteger(rawTransaction["gasPrice"])
            val value = toBigInteger(rawTransaction["value"])
            val init = rawTransaction["init"] as String
            val transaction = Transaction.createContractTransaction(from, getNonce(from, web3), gasPrice, gas, value, init)
            runAsync(web3.ethEstimateGas(transaction))
        },
        Pair("eth_getBlockByHash") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetBlockByHash(params[0] as String, params[1].toString().toBoolean()))
        },
        Pair("eth_getBlockByNumber") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(params[0] as String), params[1].toString().toBoolean()))
        },
        Pair("eth_getTransactionByHash") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetTransactionByHash(params[0] as String))
        },
        Pair("eth_getTransactionByBlockHashAndIndex") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetTransactionByBlockHashAndIndex(params[0] as String, params[1].toString().toBigInteger()))
        },
        Pair("eth_getTransactionByBlockNumberAndIndex") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf(params[0] as String), params[1].toString().toBigInteger()))
        },
        Pair("eth_getTransactionReceipt") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetTransactionReceipt(params[0] as String))
        },
        Pair("eth_getUncleByBlockHashAndIndex") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetUncleByBlockHashAndIndex(params[0] as String, params[1].toString().toBigInteger()))
        },
        Pair("eth_getUncleByBlockNumberAndIndex") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethGetUncleByBlockNumberAndIndex(DefaultBlockParameter.valueOf(params[0] as String), params[1].toString().toBigInteger()))
        },
        Pair("eth_getCompilers") { web3: Web3j, params: List<Any> -> runAsync(web3.ethGetCompilers()) },
        Pair("eth_compileSolidity") { web3: Web3j, params: List<Any> -> runAsync(web3.ethCompileSolidity(params[0] as String)) },
        Pair("eth_compileSerpent") { web3: Web3j, params: List<Any> -> runAsync(web3.ethCompileSerpent(params[0] as String)) },
        Pair("eth_newBlockFilter") { web3: Web3j, params: List<Any> -> runAsync(web3.ethNewBlockFilter()) },
        Pair("eth_newPendingTransactionFilter") { web3: Web3j, params: List<Any> -> runAsync(web3.ethNewPendingTransactionFilter()) },
        //Pair("eth_newFilter") { web3: Web3j, params: List<Any> -> web3.ethNewFilter() },
//            Pair("eth_uninstallFilter") { web3: Web3j, params: List<Any> -> web3.ethUninstallFilter() },
//            Pair("eth_getFilterChanges") { web3: Web3j, params: List<Any> -> web3.ethGetFilterChanges() },
//            Pair("eth_getFilterLogs") { web3: Web3j, params: List<Any> -> web3.ethGetFilterLogs() },
//            Pair("eth_getLogs") { web3: Web3j, params: List<Any> -> web3.ethGetLogs() },
        Pair("eth_submitWork") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethSubmitWork(params[0] as String, params[1] as String, params[2] as String))
        },
        Pair("eth_submitHashrate") { web3: Web3j, params: List<Any> ->
            runAsync(web3.ethSubmitHashrate(params[0] as String, params[1] as String))
        },
        Pair("db_putString") { web3: Web3j, params: List<Any> ->
            runAsync(web3.dbPutString(params[0] as String, params[1] as String, params[2] as String))
        },
        Pair("db_getString") { web3: Web3j, params: List<Any> ->
            runAsync(web3.dbGetString(params[0] as String, params[1] as String))
        },
        Pair("db_putHex") { web3: Web3j, params: List<Any> ->
            runAsync(web3.dbPutHex(params[0] as String, params[1] as String, params[2] as String))
        },
        Pair("db_getHex") { web3: Web3j, params: List<Any> -> runAsync(web3.dbGetHex(params[0] as String, params[1] as String)) },
        Pair("txpool_status") { web3: Web3j, params: List<Any> -> runAsync(web3.txPoolStatus()) }
    )

    fun execute(testStep: Web3TestStep, web3Node: Web3j): Any {
        return functions[testStep.method]?.let {
            it(web3Node, testStep.getParams())
        } ?: error("Function ${testStep.method} is not supported")
    }

    private fun runAsync(function: Request<*, out Response<out Any>>): Any? = function.sendAsync().get(5, TimeUnit.MINUTES)

    private fun createFunction(data: Map<String, Any>): Function {
        val params = (data["params"] as List<Any>).map { convertParam(it) }
        val typeReferences = (data["returns"] as List<Any>).map {
            typeReferencesMap.get(it.toString()) ?: error("No type reference found for type '$it'. Supported types are: $typeReferencesMap")
        }
        return Function(
            data["function"] as String,
            params,
            typeReferences
        )
    }

    private fun convertParam(data: Any?): Type<out Any> =
        when (data) {
            is Type<*> -> data
            is Collection<*> -> StaticStruct(data.map { convertParam(it) }.toMutableList())
            is String -> Utf8String(data)
            else -> throw IllegalArgumentException("Invalid type found in 'params' with value '$data'. Must be a web3 type, List or String")
        }

    private fun toBigInteger(value: Any?): BigInteger? =
        if (value is BigInteger) value else value?.toString()?.toBigInteger()

    private fun getNonce(address: String?, web3: Web3j): BigInteger {
        val ethGetTransactionCount: EthGetTransactionCount = web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
        return ethGetTransactionCount.transactionCount
    }

    private fun getBlockParameter(blockParam: Any?): DefaultBlockParameter {
        return when (blockParam) {
            is BigInteger -> DefaultBlockParameter.valueOf(blockParam)
            is String -> DefaultBlockParameter.valueOf(blockParam)
            else -> DefaultBlockParameterName.LATEST
        }
    }

}