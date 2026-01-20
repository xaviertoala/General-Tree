package ec.edu.espoch.modelo;

import java.util.LinkedList;
import java.util.Queue;

public class ArbolGeneral<L extends Comparable<L>> {

    public Nodo<L> raiz;

    public ArbolGeneral() {
        this.raiz = null;
    }

    public Nodo<L> getRaiz() {
        return raiz;
    }

    public void insertar(L valor) {
        if (raiz == null) {
            raiz = new Nodo<>(valor);
        } else {
            insertarEnNivel(raiz, valor);
        }
    }

    private void insertarEnNivel(Nodo<L> actual, L valor) {
        Queue<Nodo<L>> cola = new LinkedList<>();
        cola.add(actual);

        while (!cola.isEmpty()) {
            Nodo<L> node = cola.poll();

            // Limitamos a 3 hijos por nodo para mantener una visualización limpia
            // 
            if (node.hijos.size() < 3) {
                node.hijos.add(new Nodo<>(valor));
                return;
            } else {
                cola.addAll(node.hijos);
            }
        }
    }

    public void eliminar(L valor) {
        if (raiz == null)
            return;
        if (raiz.valor.equals(valor)) {
            raiz = null;
            return;
        }
        eliminarRecursivo(raiz, valor);
    }

    private boolean eliminarRecursivo(Nodo<L> actual, L valor) {
        for (int i = 0; i < actual.hijos.size(); i++) {
            if (actual.hijos.get(i).valor.equals(valor)) {
                actual.hijos.remove(i);
                return true;
            }
            if (eliminarRecursivo(actual.hijos.get(i), valor)) {
                return true;
            }
        }
        return false;
    }
}

            