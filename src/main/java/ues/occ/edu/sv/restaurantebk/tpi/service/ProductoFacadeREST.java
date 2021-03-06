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
import ues.occ.edu.sv.restaurantebk.tpi.entities.Producto;
import ues.occ.edu.sv.restaurantebk.tpi.facades.CategoriaFacade;
import ues.occ.edu.sv.restaurantebk.tpi.facades.ProductoFacade;
import ues.occ.edu.sv.restaurantebk.tpi.services.fatherClassVerify;

/**
 *
 * @author enrique
 */
@Stateless
@Path("producto")
public class ProductoFacadeREST extends fatherClassVerify implements Serializable {

    @Inject
    ProductoFacade productoFacade;
    @Inject
    CategoriaFacade categoriaFacade;

    /**
     * Lista todos los objetos de producto, pidiendo como autorizacion el JWT
     * que viene como headers en la peticion http
     *
     * @param JWT
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Producto> findAll(@HeaderParam("JWT") String JWT) {
        try {
            if (JWT != null) {
                DecodedJWT token = verificarJWT(JWT);
                if (token != null) {
                    return productoFacade.findAll();
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
     * Se crean los productos con el objeto json referenciado en jsonString y
     * como autorizacion para crear objetos el JWT que se accede atraves de los
     * headers
     *
     * @param jsonString
     * @param JWT
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearNuevo(String jsonString, @HeaderParam("JWT") String JWT) {
        try {
            if (JWT != null) {
                DecodedJWT token = verificarJWT(JWT);
                if (token != null) {
                    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                    if (isNullOrEmpty(json.get("idCategoria").getAsString())
                            && isNullOrEmpty(json.get("nombreProducto").getAsString())
                            && isNullOrEmpty(json.get("precio").getAsString())
                            && isNullOrEmpty(json.get("esPreparado").getAsString())) {
                        if (productoFacade.noNombresIguales(json.get("nombreProducto").getAsString())) {
                            if (productoFacade.create(new Producto(null, (categoriaFacade.find(json.get("idCategoria").getAsInt())),
                                    json.get("nombreProducto").getAsString(), json.get("precio").getAsDouble(),
                                    json.get("esPreparado").getAsBoolean()))) {
                                return Response.status(Response.Status.CREATED).header("mensaje", "producto creado con exito").build();
                            } else {
                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "no se pudo crear el producto").build();
                            }
                        } else {
                            return Response.status(Response.Status.CONFLICT).header("mensaje", "Nombre del producto ya existe").build();
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
     * Metodo para editar los productos no se pueden repetir nombres, para la
     * creación de productos se necesita el JWT y el objeto json que viene desde
     * el front, con las caracteristicas de un pruducto, es necesario que el
     * objeto cumpla con todas las normativas de no null
     *
     * @param jsonString
     * @param JWT
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response edit(String jsonString, @HeaderParam("JWT") String JWT) {
        try {
            if (JWT != null) {
                DecodedJWT token = verificarJWT(JWT);
                if (token != null) {
                    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                    if (isNullOrEmpty(json.get("idProducto").getAsString())
                            && isNullOrEmpty(json.get("idCategoria").getAsString())
                            && isNullOrEmpty(json.get("nombreProducto").getAsString())
                            && isNullOrEmpty(json.get("precio").getAsString())
                            && isNullOrEmpty(json.get("esPreparado").getAsString())) {
                        if (productoFacade.edit(new Producto(json.get("idProducto").getAsInt(),
                                (categoriaFacade.find(json.get("idCategoria").getAsInt())),
                                json.get("nombreProducto").getAsString(), json.get("precio").getAsDouble(),
                                json.get("esPreparado").getAsBoolean()))) {
                            return Response.status(Response.Status.OK).header("mensaje", "Se modifico con exito").build();
                        } else {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "No se pudo modificar el producto").build();
                        }
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).header("mensaje", "los campos vinieron vacios").build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "token no valido").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No valido sin JWT").build();
            }
        } catch (JsonSyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error dentro del servidor " + e).build();
        }
    }

    /**
     * Se pide el pathparam id para eliminar el producto, siempre con el jwt que
     * es necesario que vaya en el header
     *
     * @param id
     * @param JWT
     * @return
     */
    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id, @HeaderParam("JWT") String JWT) {
        try {
            if (JWT != null) {
                System.out.println("........................................" + id);
                DecodedJWT token = verificarJWT(JWT);
                if (token != null) {
                    if (productoFacade.remove(productoFacade.find((Integer) Integer.parseInt(id.trim())))) {
                        return Response.status(Response.Status.OK).header("mensaje", "El producto se borro con exito").build();
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "no se pudo borrar la categoria").build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "token no autorizado").build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No es posible sin token").build();
            }
        } catch (JsonSyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error dentro del servidor " + e).build();
        }
    }

    /**
     * Es necesario espeficicar desde donde hasta donde, con el JWT se da
     * autorizacion a la peticion get y se devulven en formato json los objetos
     * de esta clase
     *
     * @param from
     * @param to
     * @param JWT
     * @return
     */
    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @HeaderParam("JWT") String JWT) {
        try {
            if (JWT != null) {
                DecodedJWT token = verificarJWT(JWT);
                if (token != null) {
                    return Response.status(Response.Status.OK).header("mensaje", "Hay te van").entity(productoFacade.findRange(from, to)).build();
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "token no valido").entity(Collections.EMPTY_LIST).build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).header("mensaje", "No valido sin JWT").entity(Collections.EMPTY_LIST).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("mensaje", "Error dentro del servidor " + e).entity(Collections.EMPTY_LIST).build();
        }
    }

}
