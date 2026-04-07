package com.tienda_losLunes.service;

import com.tienda.domain.Categoria;
import com.tienda.repository.CategoriaRepository;
import com.tienda.service.FirebaseStorageService;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CategoriaService {
    
    //Permite crear una unica instancia de CategoriaRepository, y la crea de forma automatica
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Transactional(readOnly = true)
    public List<Categoria> getCategorias (boolean activo) {
        if (activo) { //Solo activos...
            return categoriaRepository.findByActivoTrue();
        }
        return categoriaRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Categoria> getCategoria (Integer idCategoria) {
        return categoriaRepository.findById(idCategoria);
    }
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    
    @Transactional
    public void save (Categoria categoria, MultipartFile imagenFile) {
        categoria = categoriaRepository.save(categoria);
        if (!imagenFile.isEmpty()) { //Si no esta vacio... pasaron una imagen...
        try{
            String rutaImagen = firebaseStorageService.uploadImage(imagenFile, "categoria", categoria.getIdCategoria());
            categoria.setRutaImagen(rutaImagen);
            categoriaRepository.save(categoria);
        } catch (IOException e) {
             throw new IllegalStateException("No se puede guardar la categoria.", e);
        }
        }
    }
    
    @Transactional
    public void delete (Integer idCategoria) {
        //Verifica si la categoria existe antes de intentar eliminarlo
        if (!categoriaRepository.existsById(idCategoria))
        {
            //Lanza una excepcion para indicar que el usuario no fue encontrado
    throw new IllegalArgumentException("La categoria con el ID " + idCategoria + " no existe.");
        }
        try{
            categoriaRepository.deleteById(idCategoria);
        } catch (DataIntegrityViolationException e)
        {
            //Lanza una nueva excepcion para encapsular el problema de integridad de datos
            throw new IllegalStateException("No se puede eliminar la categoria. Tiene datos asociados.", e);
        }
    }
    

}
