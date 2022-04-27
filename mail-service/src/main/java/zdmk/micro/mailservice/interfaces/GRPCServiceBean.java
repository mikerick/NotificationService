package zdmk.micro.mailservice.interfaces;


/**
 * A marker interface for GPRC service beans.
 * as they are created as *GrpcImpl extends *.*ImplBase
 * and implemented by *ImplBase io.grpc.BindableService is not a bean
 * (perhaps it'll be more beautiful to use an annotation and a starter but why...)
 */
public interface GRPCServiceBean {
}
