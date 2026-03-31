package com.tienda_losLunes.service;

import com.tienda_losLunes.domain.Producto;
import com.tienda_losLunes.repository.ProductoRepository;
import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {
    
    //Permite crear una unica instancia de ProductoRepository, y la crea de forma automatica
    @Autowired
    private ProductoRepository productoRepository;
    
    @Transactional(readOnly = true)
    public List<Producto> getProductos (boolean activo) {
        if (activo) { //Solo activos...
            return productoRepository.findByActivoTrue();
        }
        return productoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Producto> getProducto (Integer idProducto) {
        return productoRepository.findById(idProducto);
    }
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    
    @Transactional
    public void save (Producto producto, MultipartFile imagenFile) {
        producto = productoRepository.save(producto);
        if (!imagenFile.isEmpty()) { //Si no esta vacio... pasaron una imagen...
        try{
            String rutaImagen = firebaseStorageService.uploadImage(imagenFile, "producto", producto.getIdProducto());
            producto.setRutaImagen(rutaImagen);
            productoRepository.save(producto);
        } catch (IOException e) {
             throw new IllegalStateException("No se puede guardar la producto.", e);
        }
        }
    }
    
    @Transactional
    public void delete (Integer idProducto) {
        //Verifica si la producto existe antes de intentar eliminarlo
        if (!productoRepository.existsById(idProducto))
        {
            //Lanza una excepcion para indicar que el usuario no fue encontrado
    throw new IllegalArgumentException("La producto con el ID " + idProducto + " no existe.");
        }
        try{
            productoRepository.deleteById(idProducto);
        } catch (DataIntegrityViolationException e)
        {
            //Lanza una nueva excepcion para encapsular el problema de integridad de datos
            throw new IllegalStateException("No se puede eliminar la producto. Tiene datos asociados.", e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Producto> consultaDerivada(BigDecimal precioInf, BigDecimal precioSup) {
        return productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioInf, precioSup);
    }
    
    @Transactional(readOnly = true)
    public List<Producto> consultaJPQL(BigDecimal precioInf, BigDecimal precioSup) {
        return productoRepository.consultaJPQL(precioInf, precioSup);
    }
    
    @Transactional(readOnly = true)
    public List<Producto> consultaSQL(BigDecimal precioInf, BigDecimal precioSup){
        return productoRepository.consultaSQL(precioInf, precioSup);
    }
    

}
