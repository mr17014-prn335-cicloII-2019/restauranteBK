/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ues.occ.edu.sv.restaurantebk.tpi.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import ues.occ.edu.sv.restaurantebk.tpi.cors.verificacion;
import ues.occ.edu.sv.restaurantebk.tpi.entities.DetalleOrden;
import ues.occ.edu.sv.restaurantebk.tpi.entities.DetalleOrdenPK;
import ues.occ.edu.sv.restaurantebk.tpi.facades.DetalleOrdenFacade;
import ues.occ.edu.sv.restaurantebk.tpi.facades.OrdenFacade;
import ues.occ.edu.sv.restaurantebk.tpi.facades.ProductoFacade;

/**
 *
 * @author enrique
 */
@Stateless
@Path("detalleorden")
public class DetalleOrdenFacadeREST implements Serializable{
    /*Se injecta el facade de detalleOrden para poder hacer uso de los metodos que llaman a la
    persistencia*/
    @Inject
    DetalleOrdenFacade detalleOrdenFacade;
    /*
    Se injecta el Ordenfacade para poder hacer uso de los metodos que llaman a la persistencia
    */
    @Inject
    OrdenFacade ordenFacade;
    /*
    Injectamos el ProductoFacade para poder acceder a los metodos del facade de productos
    */
    @Inject
    ProductoFacade productoFacade;
    /*
    se llama a esta clase porque contiene un metodo de verificacion de jwt
    */
    @Inject
    verificacion verificacion;
    /**
     * Metodo Para Obtener La lista completa de los detalleOrdenes
     * 
     * @param JWT
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DetalleOrden> findAll(@HeaderParam("JWT") String JWT){
        try {
            if (JWT != null) {
                DecodedJWT token = verificacion.verificarJWT(JWT);
                if (token != null) {
                    return detalleOrdenFacade.findAll();
                } else {
                    return Collections.EMPTY_LIST;
                }
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }
    /**
     * Metodo Para crear un detalleOrden Solo se pide los id de la orden y de producto
     * y la cantidad y su precio unitario
     * 
     * @param jsonString
     * @param JWT
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearNuevo(String jsonString, @HeaderParam("JWT") String JWT){
        try {
            if (JWT != null) {
                DecodedJWT token = verificacion.verificarJWT(JWT);
                if (token != null) {
                    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                    if (isNullOrEmpty(json.get("cantidad").getAsString())
                            && isNullOrEmpty(json.get("idProducto").getAsString())
                            && isNullOrEmpty(json.get("idOrden").getAsString())
                            && isNullOrEmpty(json.get("precioUnitario").getAsString())) {
                            DetalleOrdenPK detalleOrdenPK = new DetalleOrdenPK(json.get("idOrden").getAsInt(), json.get("idProducto").getAsInt());
                            DetalleOrden detalleOrden = new DetalleOrden(detalleOrdenPK, json.get("cantidad").getAsInt(), json.get("precioUnitario").getAsBigDecimal());
                        if (detalleOrdenFacade.create(detalleOrden)) {
                            return Response.status(Response.Status.CREATED).header("mensaje", "orden creada con exito").build();
                        } else {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "no se pudo crear la orden").build();
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).header("mensaje", "json vienen campos vacios que son requeridos").build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "Token no valido").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No valido sin JWT").build();
            }
        } catch (JsonSyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error del server " + e).build();
        }
    }
    
    /**
     * Metodo Para editar por el momento no tiene restriccion de edicion
     * es decir que El PK es el unico dato que no es identico pero como las 
     * ordenes puden cambiar, si se encuentran errores se modifica
     * 
     * @param jsonString
     * @param JWT
     * @return 
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response edit(String jsonString, @HeaderParam("JWT") String JWT){
        try {
            if (JWT != null) {
                DecodedJWT token = verificacion.verificarJWT(JWT);
                if (token != null) {
                    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                    if (isNullOrEmpty(json.get("cantidad").getAsString())
                            && isNullOrEmpty(json.get("idProducto").getAsString())
                            && isNullOrEmpty(json.get("idOrden").getAsString())
                            && isNullOrEmpty(json.get("precioUnitario").getAsString())) {
                            DetalleOrdenPK detalleOrdenPK = new DetalleOrdenPK(json.get("idOrden").getAsInt(), json.get("idProducto").getAsInt());
                            DetalleOrden detalleOrden = new DetalleOrden(detalleOrdenPK, json.get("cantidad").getAsInt(), json.get("precioUnitario").getAsBigDecimal());
                        if (detalleOrdenFacade.edit(detalleOrden)) {
                            return Response.status(Response.Status.CREATED).header("mensaje", "orden creada con exito").build();
                        } else {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "no se pudo crear la orden").build();
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).header("mensaje", "json vienen campos vacios que son requeridos").build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "Token no valido").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No valido sin JWT").build();
            }
        } catch (JsonSyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error del server " + e).build();
        }
    }
    
    /**
     * Metodo para eliminar el detallo orden, se pide el id del detalle orden para buscar el objeto
     * y eliminarlo, es necesario el header JWT para la autorizacion de eliminacion
     * 
     * @param id
     * @param JWT
     * @return 
     */
    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id, @HeaderParam("JWT") String JWT){
        try {
            if(JWT != null){
                System.out.println("........................................"+id);
                DecodedJWT token = verificacion.verificarJWT(JWT);
                if(token != null){
                    if(detalleOrdenFacade.remove(detalleOrdenFacade.find((Integer) Integer.parseInt(id.trim())))){
                        return Response.status(Response.Status.OK).header("mensaje", "se borro con exito").build();
                    }else{
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "no se pudo borrar la categoria").build();
                    }
                }else{
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "token no autorizado").build();
                }
            }else{
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No es posible sin token").build();
            }
        } catch (JsonSyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error dentro del servidor "+e).build();
        }
    }
    /**
     * Se verifica si un string esta vacio
     * 
     * @param str
     * @return 
     */
    public static boolean isNullOrEmpty(String str) {
        return ((str != null) ? (!str.trim().isEmpty()) : (false));
    }
   
}
