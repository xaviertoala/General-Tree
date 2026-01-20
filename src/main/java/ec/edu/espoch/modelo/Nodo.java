package ec.edu.espoch.modelo;

import java.util.ArrayList;
import java.util.List;

public class Nodo<L> {
    public L valor;
    public List<Nodo<L>> hijos;

    public Nodo(L valor) {
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }
}
