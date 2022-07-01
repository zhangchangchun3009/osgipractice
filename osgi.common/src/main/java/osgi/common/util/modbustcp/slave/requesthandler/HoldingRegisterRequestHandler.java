
package osgi.common.util.modbustcp.slave.requesthandler;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.modbus.ExceptionCode;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import osgi.common.util.ByteUtil;
import osgi.common.util.modbustcp.slave.db.ModbusPoolDBMS;
import osgi.common.util.modbustcp.slave.db.TransactionException;
import osgi.common.util.modbustcp.slave.event.ModbusSlaveEvent;
import osgi.common.util.modbustcp.slave.event.ModbusSlaveEventType;
import osgi.common.util.modbustcp.slave.event.ModbusSlaveRWEventDispatcher;

/**
 * The Class HoldingRegisterRequestHandler.
 *
 * @author zcc
 * @since 2021年12月13日
 */
public class HoldingRegisterRequestHandler implements ServiceRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoldingRegisterRequestHandler.class);

    @Override
    public void onReadHoldingRegisters(
            ServiceRequest<ReadHoldingRegistersRequest, ReadHoldingRegistersResponse> service) {
        int unitId = service.getUnitId();
        ReadHoldingRegistersRequest readRequest = service.getRequest();
        int address = readRequest.getAddress();
        int quantity = readRequest.getQuantity();
        ModbusSlaveEvent event;
        try {
            short[] values = ModbusPoolDBMS.readHoldingRegister(address, quantity, String.valueOf(unitId));
            ByteBuf registers = Unpooled.wrappedBuffer(ByteUtil.shortArr2ByteArr(values));
            service.sendResponse(new ReadHoldingRegistersResponse(registers));
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_READ_SUCCESS, unitId, address, quantity);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("pool.readHoldingRegister err", e);
            service.sendException(ExceptionCode.IllegalDataAddress);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_READ_FAIL, unitId, address, quantity);
        } catch (TimeoutException e) {
            LOGGER.error("pool.readHoldingRegister err", e);
            service.sendException(ExceptionCode.SlaveDeviceBusy);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_READ_FAIL, unitId, address, quantity);
        }
        ModbusSlaveRWEventDispatcher.dispatch(event);
    }

    @Override
    public void onWriteSingleRegister(ServiceRequest<WriteSingleRegisterRequest, WriteSingleRegisterResponse> service) {
        WriteSingleRegisterRequest request = service.getRequest();
        int unitId = service.getUnitId();
        int transactionId = service.getTransactionId();
        int address = request.getAddress();
        ModbusSlaveEvent event;
        int value = request.getValue();
        try {
            ModbusPoolDBMS.writeSingleRegister(address, (short) value, transactionId, String.valueOf(unitId));
            service.sendResponse(new WriteSingleRegisterResponse(address, value));
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_SINGLE_WRITE_SUCCESS, unitId, address, 1);
        } catch (TransactionException e) {
            LOGGER.error("pool.onWriteSingleRegister err", e);
            service.sendException(ExceptionCode.SlaveDeviceBusy);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_SINGLE_WRITE_FAIL, unitId, address, 1);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("pool.onWriteSingleRegister err", e);
            service.sendException(ExceptionCode.IllegalDataAddress);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_SINGLE_WRITE_FAIL, unitId, address, 1);
        }
        ModbusSlaveRWEventDispatcher.dispatch(event);
    }

    @Override
    public void onWriteMultipleRegisters(
            ServiceRequest<WriteMultipleRegistersRequest, WriteMultipleRegistersResponse> service) {
        WriteMultipleRegistersRequest request = service.getRequest();
        int unitId = service.getUnitId();
        int transactionId = service.getTransactionId();
        int address = request.getAddress();
        byte[] value = ByteBufUtil.getBytes(request.getValues());
        int quantity = request.getQuantity();
        ModbusSlaveEvent event;
        try {
            ModbusPoolDBMS.writeMultiRegister(address, quantity, ByteUtil.byteArr2ShortArr(value), transactionId,
                    String.valueOf(unitId));
            service.sendResponse(new WriteMultipleRegistersResponse(address, quantity));
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_MULTIPLE_WRITE_SUCCESS, unitId, address,
                    quantity);
        } catch (TransactionException e) {
            LOGGER.error("pool.onWriteMultipleRegisters err", e);
            service.sendException(ExceptionCode.SlaveDeviceBusy);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_MULTIPLE_WRITE_FAIL, unitId, address,
                    quantity);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("pool.onWriteMultipleRegisters err", e);
            service.sendException(ExceptionCode.IllegalDataAddress);
            event = new ModbusSlaveEvent(ModbusSlaveEventType.HOLDINGREGISTER_MULTIPLE_WRITE_FAIL, unitId, address,
                    quantity);
        }
        ModbusSlaveRWEventDispatcher.dispatch(event);
    }

}
