package com.qual.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qual.store.converter.ProductConverter;
import com.qual.store.converter.lazyConverter.ProductLazyConverter;
import com.qual.store.dto.ProductDto;
import com.qual.store.dto.paginated.PaginatedProductResponse;
import com.qual.store.dto.request.ProductRequestDto;
import com.qual.store.model.Product;
import com.qual.store.repository.AppUserRepository;
import com.qual.store.service.CategoryService;
import com.qual.store.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private ProductConverter productConverter;

    @InjectMocks
    private ProductController productController;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void getAllProductsTest() throws Exception {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        ProductDto productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Test Product");

        List<Product> productList = new ArrayList<>();
        productList.add(product);

        // when
        when(productService.getAllProducts()).thenReturn(productList);
        when(productConverter.convertModelToDto(product)).thenReturn(productDto);

        // then
        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(productDto.getId()))
                .andExpect(jsonPath("$[0].name").value(productDto.getName()))
                .andExpect(jsonPath("$.length()").value(productList.size()));

        verify(productService, times(1)).getAllProducts();
        verify(productConverter, times(1)).convertModelToDto(product);
    }

    @Test
    public void getProductByIdTest() throws Exception {
        // given
        Long productId = 1L;

        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Test Product");

        // when
        when(productService.getProductById(productId)).thenReturn(productDto);

        // then
        mockMvc.perform(get("/api/products/{productId}", productId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productDto.getId()))
                .andExpect(jsonPath("$.name").value(productDto.getName()));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void testAddProductCategory() throws Exception {
        // given
        Long categoryId = 1L;
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name("Test Product")
                .description("Test Description")
                .price(10.0)
                .unitsInStock(100L)
                .discountPercentage(20.0)
                .build();

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        ProductDto productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Test Product");

        // when
        when(productService.saveProductCategory(eq(productRequestDto), eq(categoryId))).thenReturn(product);
        when(productConverter.convertModelToDto(product)).thenReturn(productDto);

        // then
        mockMvc.perform(post("/api/products/category/{categoryId}", categoryId)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("name", productRequestDto.getName())
                        .param("description", productRequestDto.getDescription())
                        .param("price", String.valueOf(productRequestDto.getPrice()))
                        .param("unitsInStock", String.valueOf(productRequestDto.getUnitsInStock()))
                        .param("discountPercentage", String.valueOf(productRequestDto.getDiscountPercentage())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).saveProductCategory(eq(productRequestDto), eq(categoryId));
        verify(productConverter, times(1)).convertModelToDto(product);
    }

    @Test
    public void updateProductTest() throws Exception {
        // given
        Long productId = 1L;
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name("Updated Product")
                .description("Test Description")
                .price(10.0)
                .unitsInStock(100L)
                .discountPercentage(20.0)
                .build();

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated Product");

        ProductDto updatedProductDto = new ProductDto();
        updatedProductDto.setId(productId);
        updatedProductDto.setName("Updated Product");

        // when
        when(productService.updateProduct(anyLong(), any(ProductRequestDto.class))).thenReturn(updatedProduct);
        when(productConverter.convertModelToDto(any(Product.class))).thenReturn(updatedProductDto);

        // then
        String jsonString = asJsonString(productRequestDto);
        System.out.println("jsonString: " + jsonString);

        mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Product"));

        verify(productService, times(1)).updateProduct(eq(productId), eq(productRequestDto));
        verify(productConverter, times(1)).convertModelToDto(updatedProduct);
    }

    @Test
    void deleteProductByIdTest() throws Exception {
        // given
        Long productId = 1L;

        // when
        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("Product with id 1 deleted"));

        // then
        verify(productService, times(1)).deleteProductById(productId);
    }

    @Test
    void testGetProducts() throws Exception {
        // given
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "id";

        ProductDto firstProductDto = new ProductDto();
        firstProductDto.setId(1L);
        firstProductDto.setName("Test Product 1");

        ProductDto secondProductDto = new ProductDto();
        secondProductDto.setId(2L);
        secondProductDto.setName("Test Product 2");

        List<ProductDto> products = new ArrayList<>();
        products.add(firstProductDto);
        products.add(secondProductDto);

        long numberOfItems = 2L;
        int numberOfPages = 1;
        PaginatedProductResponse paginatedResponse = PaginatedProductResponse.builder()
                .products(products)
                .numberOfItems(numberOfItems)
                .numberOfPages(numberOfPages)
                .build();

        // when
        when(productService.getProducts(pageNumber, pageSize, sortBy)).thenReturn(paginatedResponse);

        // then
        mockMvc.perform(get("/api/products/display")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$.products[1].name").value("Test Product 2"))
                .andExpect(jsonPath("$.numberOfItems").value(numberOfItems))
                .andExpect(jsonPath("$.numberOfPages").value(numberOfPages));

        verify(productService, times(1)).getProducts(pageNumber, pageSize, sortBy);
    }

    @Test
    void getProductsByCategoryTest() throws Exception {
        // given
        Long categoryId = 1L;

        // when
        when(productService.findProductsByCategory(categoryId)).thenReturn(new ArrayList<>());
        when(productConverter.convertModelToDto(any())).thenReturn(new ProductDto());

        // then
        mockMvc.perform(get("/api/products/category")
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andReturn();

        verify(productService, times(1)).findProductsByCategory(categoryId);
        verify(productConverter, times(0)).convertModelToDto(any());
    }

    @AfterEach
    public void closeService() throws Exception {
        closeable.close();
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}